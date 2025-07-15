package libV2ray

import (
	"net"
)

// GetFreePorts asks the kernel for free open ports that are ready to use.
func GetFreePorts(count int) ([]int, error) {
	var ports []int
	for range count {
		addr, err := net.ResolveTCPAddr("tcp", "localhost:0")
		if err != nil {
			return ports, err
		}

		l, err := net.ListenTCP("tcp", addr)
		if err != nil {
			return ports, err
		}
		ports = append(ports, l.Addr().(*net.TCPAddr).Port)
		l.Close()
	}
	return ports, nil
}

// GetFreePort asks the kernel for a single free open port that is ready to use.
func GetFreePort() (int, error) {
	addr, err := net.ResolveTCPAddr("tcp", "localhost:0")
	if err != nil {
		return 0, err
	}

	l, err := net.ListenTCP("tcp", addr)
	if err != nil {
		return 0, err
	}
	port := l.Addr().(*net.TCPAddr).Port
	l.Close()
	return port, nil
}
