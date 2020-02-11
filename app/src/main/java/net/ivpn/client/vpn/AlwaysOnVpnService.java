package net.ivpn.client.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.support.annotation.Nullable;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.local.PermissionActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class AlwaysOnVpnService extends VpnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlwaysOnVpnService.class);

    @Inject
    VpnBehaviorController vpnBehaviorController;

    @Override
    public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        if (intent == null || intent.getComponent() == null || !intent.getComponent().getPackageName().equals(getPackageName())) {
            LOGGER.info("Service started by Always-on VPN feature");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                vpnBehaviorController.connectActionByRules();
            } else {
                Intent vpnIntent = new Intent(this, PermissionActivity.class);
                vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(vpnIntent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
