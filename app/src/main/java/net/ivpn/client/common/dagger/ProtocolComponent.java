package net.ivpn.client.common.dagger;

import net.ivpn.client.vpn.controller.OpenVpnBehavior;
import net.ivpn.client.vpn.controller.WireGuardBehavior;

import dagger.Subcomponent;

@Subcomponent
public interface ProtocolComponent {

    @Subcomponent.Factory
    interface Factory {
        ProtocolComponent create();
    }

    WireGuardBehavior getWireGuardBehavior();

    OpenVpnBehavior getOpenVpnBehavior();
}
