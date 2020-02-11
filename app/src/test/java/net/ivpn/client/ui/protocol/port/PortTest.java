package net.ivpn.client.ui.protocol.port;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PortTest {

    @Test
    public void nextOpenvpn() {
        assertEquals(Port.UDP_2049.next(), Port.UDP_2050);
        assertEquals(Port.TCP_80.next(), Port.UDP_2049);
    }

    public void nextWg() {
        assertEquals(Port.WG_UDP_48574.next(), Port.WG_UDP_48574);
    }

    @Test
    public void toJson() {
        Port port = Port.TCP_443;
        String portJson = port.toJson();
        Port restoredPort = Port.from(portJson);
        assertEquals(port, restoredPort);
    }
}