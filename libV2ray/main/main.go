//go:build !android
// +build !android

package main

import (
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/ivpn/libV2ray/libV2ray"
)

// CallbackHandler implements the CoreCallbackHandler interface
type CallbackHandler struct{}

func (c *CallbackHandler) Startup() int {
	log.Println("V2Ray started")
	return 0
}

func (c *CallbackHandler) Shutdown() int {
	log.Println("V2Ray shutdown")
	return 0
}

func (c *CallbackHandler) OnEmitStatus(code int, message string) int {
	log.Printf("Status: [%d] %s", code, message)
	return 0
}

func main() {
	config := `{
   "log":{
      "loglevel":"debug"
   },
   "inbounds":[
      {
         "listen":"127.0.0.1",
         "protocol":"dokodemo-door",
         "port":"16661",
         "settings":{
            "address":"149.22.83.100",
            "port":15351,
            "network":"udp"
         },
         "tag":"vpn"
      }
   ],
   "outbounds":[
      {
         "protocol":"vmess",
         "settings":{
            "vnext":[
               {
                  "users":[
                     {
                        "id":"27de860d-5601-412d-8b71-baa048a94b98",
                        "security":"none",
                        "alterId":0
                     }
                  ],
                  "address":"149.22.83.102",
                  "port":2049
               }
            ]
         },
         "streamSettings":{
            "security":"tls",
            "network":"quic",
            "quicSettings":{
               "header":{
                  "type":"srtp"
               },
               "key":"",
               "security":""
            },
            "tlsSettings":{
               "serverName":"ar1.gw.inet-telecom.com"
            }
         },
         "tag":"proxy"
      }
   ]
}
`

	// Create a new CoreController with our callback handler
	controller := libV2ray.NewCoreController(&CallbackHandler{})

	// Initialize the environment (optional, depending on your needs)
	libV2ray.InitCoreEnv("", "")

	// Start V2Ray with the config
	if err := controller.StartLoop(config); err != nil {
		log.Fatalf("Failed to start V2Ray: %v", err)
	}

	// Wait for interrupt signal
	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)
	<-sigCh

	// Stop V2Ray gracefully
	if err := controller.StopLoop(); err != nil {
		log.Printf("Error stopping V2Ray: %v", err)
	}
}
