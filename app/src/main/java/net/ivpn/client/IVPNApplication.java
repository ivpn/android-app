package net.ivpn.client;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import net.ivpn.client.common.dagger.ApplicationComponent;
import net.ivpn.client.common.dagger.DaggerApplicationComponent;
import net.ivpn.client.vpn.ServiceConstants;

public class IVPNApplication extends Application implements ServiceConstants {

    private static IVPNApplication instance;

    public IVPNApplication() {
        instance = this;
    }

    public ApplicationComponent appComponent = DaggerApplicationComponent.factory().create(this);

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appComponent.provideNotificationUtil().createNotificationChannels();
        }

        appComponent.provideComponentUtil().performBaseComponentsInit();
    }

    public static IVPNApplication getApplication() {
        return instance;
    }

}