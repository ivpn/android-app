package net.ivpn.client.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.local.PermissionActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class OnBootBroadcastReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalBehaviorController.class);

    @Inject Settings settings;
    @Inject VpnBehaviorController vpnBehaviorController;

    @Override
    public void onReceive(Context context, Intent intent) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        boolean isStartOnBootEnabled = settings.isStartOnBootEnabled();
        LOGGER.info("onReceive: isStartOnBootEnabled = " + isStartOnBootEnabled);
        if (isStartOnBootEnabled) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                vpnBehaviorController.connectActionByRules();
            } else {
                Intent vpnIntent = new Intent(context, PermissionActivity.class);
                vpnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(vpnIntent);
            }
        }
    }
}