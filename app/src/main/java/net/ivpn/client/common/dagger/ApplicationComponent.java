package net.ivpn.client.common.dagger;

import android.content.Context;

import com.wireguard.android.backend.GoBackend;

import net.ivpn.client.common.alarm.GlobalWireGuardAlarm;
import net.ivpn.client.common.utils.ComponentUtil;
import net.ivpn.client.common.utils.NotificationChannelUtil;
import net.ivpn.client.ui.updates.UpdatesJobService;

import dagger.BindsInstance;
import dagger.Component;

@ApplicationScope
@Component(modules = {ApplicationSubcomponents.class, NetworkModule.class})
public interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        ApplicationComponent create(@BindsInstance Context context);
    }

    ActivityComponent.Factory provideActivityComponent();

    ProtocolComponent.Factory provideProtocolComponent();

    GoBackend provideGoBackend();

    GlobalWireGuardAlarm provideGlobalWireGuardAlarm();

    NotificationChannelUtil provideNotificationUtil();

    ComponentUtil provideComponentUtil();

    void inject(UpdatesJobService service);

    void inject(GoBackend.WireGuardVpnService service);

}