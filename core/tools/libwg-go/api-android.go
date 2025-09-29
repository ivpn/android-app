/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright © 2017-2022 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 */

package main

// #cgo LDFLAGS: -llog
// #include <android/log.h>
import "C"

import (
	"fmt"
	"log"
	"math"
	"net"
	"os"
	"os/signal"
	"runtime"
	"runtime/debug"
	"strings"
	"sync"
	"unsafe"

	"golang.org/x/sys/unix"
	"golang.zx2c4.com/wireguard/conn"
	"golang.zx2c4.com/wireguard/device"
	"golang.zx2c4.com/wireguard/ipc"
	"golang.zx2c4.com/wireguard/tun"

	core "github.com/v2fly/v2ray-core/v5"
    coreapplog "github.com/v2fly/v2ray-core/v5/app/log"
    corecommlog "github.com/v2fly/v2ray-core/v5/common/log"
	coreserial "github.com/v2fly/v2ray-core/v5/infra/conf/serial"
	corestats "github.com/v2fly/v2ray-core/v5/features/stats"

	_ "github.com/v2fly/v2ray-core/v5/main/distro/all"
)

type AndroidLogger struct {
	level C.int
	tag   *C.char
}

func cstring(s string) *C.char {
	b, err := unix.BytePtrFromString(s)
	if err != nil {
		b := [1]C.char{}
		return &b[0]
	}
	return (*C.char)(unsafe.Pointer(b))
}

func (l AndroidLogger) Printf(format string, args ...interface{}) {
	C.__android_log_write(l.level, l.tag, cstring(fmt.Sprintf(format, args...)))
}

type TunnelHandle struct {
	device *device.Device
	uapi   net.Listener
}

type V2RayInstance struct {
	coreInstance *core.Instance
	statsManager corestats.Manager
	IsRunning    bool
}

var (
	v2rayLocker  sync.Mutex
	v2rayHandles = make(map[int32]*V2RayInstance)
)

var tunnelHandles map[int32]TunnelHandle


var v2rayLogInit sync.Once

type v2rayAndroidLogWriter struct {
    logger AndroidLogger
}

func (w *v2rayAndroidLogWriter) Write(s string) error {
    if len(s) > 0 && s[len(s)-1] == '\n' {
        s = s[:len(s)-1]
    }
    w.logger.Printf("%s", s)
    return nil
}

func (w *v2rayAndroidLogWriter) Close() error { return nil }

func createAndroidLogWriter(tag string) corecommlog.WriterCreator {
    return func() corecommlog.Writer {
        return &v2rayAndroidLogWriter{logger: AndroidLogger{level: C.ANDROID_LOG_INFO, tag: cstring(tag)}}
    }
}

// androidLogWriter implements io.Writer for the standard library log package
type androidLogWriter struct{ logger AndroidLogger }

func (w androidLogWriter) Write(p []byte) (int, error) {
    s := string(p)
    if len(s) > 0 && s[len(s)-1] == '\n' {
        s = s[:len(s)-1]
    }
    w.logger.Printf("%s", s)
    return len(p), nil
}

func initV2RayLogging() {
    v2rayLogInit.Do(func() {
        tag := "IVPN/V2Ray"
        _ = coreapplog.RegisterHandlerCreator(
            coreapplog.LogType_Console,
            func(lt coreapplog.LogType, options coreapplog.HandlerCreatorOptions) (corecommlog.Handler, error) {
                return corecommlog.NewLogger(createAndroidLogWriter(tag)), nil
            },
        )

        log.SetFlags(0)
        log.SetOutput(androidLogWriter{logger: AndroidLogger{level: C.ANDROID_LOG_INFO, tag: cstring(tag)}})
        log.Printf("[V2Ray] Android logging initialized")
    })
}

func init() {
	tunnelHandles = make(map[int32]TunnelHandle)
	signals := make(chan os.Signal)
	signal.Notify(signals, unix.SIGUSR2)
	go func() {
		buf := make([]byte, os.Getpagesize())
		for {
			select {
			case <-signals:
				n := runtime.Stack(buf, true)
				if n == len(buf) {
					n--
				}
				buf[n] = 0
				C.__android_log_write(C.ANDROID_LOG_ERROR, cstring("WireGuard/GoBackend/Stacktrace"), (*C.char)(unsafe.Pointer(&buf[0])))
			}
		}
	}()
}

//export wgTurnOn
func wgTurnOn(interfaceName string, tunFd int32, settings string) int32 {
	tag := cstring("WireGuard/GoBackend/" + interfaceName)
	logger := &device.Logger{
		Verbosef: AndroidLogger{level: C.ANDROID_LOG_DEBUG, tag: tag}.Printf,
		Errorf:   AndroidLogger{level: C.ANDROID_LOG_ERROR, tag: tag}.Printf,
	}

	tun, name, err := tun.CreateUnmonitoredTUNFromFD(int(tunFd))
	if err != nil {
		unix.Close(int(tunFd))
		logger.Errorf("CreateUnmonitoredTUNFromFD: %v", err)
		return -1
	}

	logger.Verbosef("Attaching to interface %v", name)
	device := device.NewDevice(tun, conn.NewStdNetBind(), logger)

	err = device.IpcSet(settings)
	if err != nil {
		unix.Close(int(tunFd))
		logger.Errorf("IpcSet: %v", err)
		return -1
	}
	device.DisableSomeRoamingForBrokenMobileSemantics()

	var uapi net.Listener

	uapiFile, err := ipc.UAPIOpen(name)
	if err != nil {
		logger.Errorf("UAPIOpen: %v", err)
	} else {
		uapi, err = ipc.UAPIListen(name, uapiFile)
		if err != nil {
			uapiFile.Close()
			logger.Errorf("UAPIListen: %v", err)
		} else {
			go func() {
				for {
					conn, err := uapi.Accept()
					if err != nil {
						return
					}
					go device.IpcHandle(conn)
				}
			}()
		}
	}

	err = device.Up()
	if err != nil {
		logger.Errorf("Unable to bring up device: %v", err)
		uapiFile.Close()
		device.Close()
		return -1
	}
	logger.Verbosef("Device started")

	var i int32
	for i = 0; i < math.MaxInt32; i++ {
		if _, exists := tunnelHandles[i]; !exists {
			break
		}
	}
	if i == math.MaxInt32 {
		logger.Errorf("Unable to find empty handle")
		uapiFile.Close()
		device.Close()
		return -1
	}
	tunnelHandles[i] = TunnelHandle{device: device, uapi: uapi}
	return i
}

//export wgTurnOff
func wgTurnOff(tunnelHandle int32) {
	handle, ok := tunnelHandles[tunnelHandle]
	if !ok {
		return
	}
	delete(tunnelHandles, tunnelHandle)
	if handle.uapi != nil {
		handle.uapi.Close()
	}
	handle.device.Close()
}

//export wgGetSocketV4
func wgGetSocketV4(tunnelHandle int32) int32 {
	handle, ok := tunnelHandles[tunnelHandle]
	if !ok {
		return -1
	}
	bind, _ := handle.device.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd4()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export wgGetSocketV6
func wgGetSocketV6(tunnelHandle int32) int32 {
	handle, ok := tunnelHandles[tunnelHandle]
	if !ok {
		return -1
	}
	bind, _ := handle.device.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd6()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export wgGetConfig
func wgGetConfig(tunnelHandle int32) *C.char {
	handle, ok := tunnelHandles[tunnelHandle]
	if !ok {
		return nil
	}
	settings, err := handle.device.IpcGet()
	if err != nil {
		return nil
	}
	return C.CString(settings)
}

//export wgVersion
func wgVersion() *C.char {
	info, ok := debug.ReadBuildInfo()
	if !ok {
		return C.CString("unknown")
	}
	for _, dep := range info.Deps {
		if dep.Path == "golang.zx2c4.com/wireguard" {
			parts := strings.Split(dep.Version, "-")
			if len(parts) == 3 && len(parts[2]) == 12 {
				return C.CString(parts[2][:7])
			}
			return C.CString(dep.Version)
		}
	}
	return C.CString("unknown")
}

//export wgV2rayStart
func wgV2rayStart(jsonConfig *C.char) C.int {
	v2rayLocker.Lock()
	defer v2rayLocker.Unlock()

    initV2RayLogging()

	configStr := C.GoString(jsonConfig)

	log.Printf("[V2Ray] Initializing core...")
	config, err := coreserial.LoadJSONConfig(strings.NewReader(configStr))
	if err != nil {
		log.Printf("[V2Ray] Configuration error: %v", err)
		return -1
	}

	coreInstance, err := core.New(config)
	if err != nil {
		log.Printf("[V2Ray] Core initialization failed: %v", err)
		return -1
	}

    var statsManager corestats.Manager
    if feature := coreInstance.GetFeature(corestats.ManagerType()); feature != nil {
        if sm, ok := feature.(corestats.Manager); ok {
            statsManager = sm
        }
    }

	instance := &V2RayInstance{
		coreInstance: coreInstance,
		statsManager: statsManager,
		IsRunning:    true,
	}

	log.Printf("[V2Ray] Starting core...")
	if err := coreInstance.Start(); err != nil {
		instance.IsRunning = false
		log.Printf("[V2Ray] Startup failed: %v", err)
		return -1
	}

    var handle int32
    // Start from 1 to avoid returning 0 which callers may treat as invalid,-1
    for handle = 1; handle < math.MaxInt32; handle++ {
		if _, exists := v2rayHandles[handle]; !exists {
			break
		}
	}
    if handle == math.MaxInt32 {
		coreInstance.Close()
		log.Printf("[V2Ray] No available handles")
        return C.int(-1)
	}

	v2rayHandles[handle] = instance
    log.Printf("[V2Ray] Core started successfully with handle %d", handle)
    return C.int(handle)
}

//export wgV2rayStop
func wgV2rayStop(handle C.int) C.int {
	v2rayLocker.Lock()
	defer v2rayLocker.Unlock()

    h := int32(handle)
    instance, ok := v2rayHandles[h]
	if !ok || instance == nil {
        log.Printf("[V2Ray] No running instance for handle %d", h)
        return C.int(0)
	}

	if !instance.IsRunning {
        delete(v2rayHandles, h)
        return C.int(0)
	}

    log.Printf("[V2Ray] Stopping core for handle %d...", h)
	instance.coreInstance.Close()
	instance.IsRunning = false
    delete(v2rayHandles, h)

	log.Printf("[V2Ray] Core stopped successfully")
    return C.int(0)
}

//export wgGetFreePort
func wgGetFreePort() C.int {
	addr, err := net.ResolveTCPAddr("tcp", "localhost:0")
	if err != nil {
		return -1
	}

	l, err := net.ListenTCP("tcp", addr)
	if err != nil {
		return -1
	}
	port := l.Addr().(*net.TCPAddr).Port
	l.Close()
	return C.int(port)
}

//export wgV2rayIsRunning
func wgV2rayIsRunning() C.int {
	v2rayLocker.Lock()
	defer v2rayLocker.Unlock()

	for _, instance := range v2rayHandles {
		if instance != nil && instance.IsRunning {
			return 1
		}
	}
	return 0
}

func main() {}