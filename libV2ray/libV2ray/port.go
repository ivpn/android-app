package libV2ray

import (
	"net"
)

// GetFreePort asks the kernel for free open ports that are ready to use.

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
