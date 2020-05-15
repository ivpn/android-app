package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.network.OnNetworkFeatureStateChanged;
import net.ivpn.client.ui.privateemails.PrivateEmailsRecyclerViewAdapter;
import net.ivpn.client.ui.serverlist.ServersRecyclerViewAdapter;
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewAdapter;
import net.ivpn.client.ui.serverlist.fastest.OnFastestSettingChangedListener;
import net.ivpn.client.ui.split.OnApplicationItemSelectionChangedListener;
import net.ivpn.client.ui.split.SplitTunnelingRecyclerViewAdapter;
import net.ivpn.client.ui.split.data.ApplicationItem;
import net.ivpn.client.v2.network.NetworkRecyclerViewAdapter;
import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.vpn.model.WifiItem;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class RecyclerViewItemsBindingAdapter {

    @BindingAdapter("app:items")
    public static void setItems(RecyclerView recyclerView, List<Server> items) {
        ServersRecyclerViewAdapter adapter = (ServersRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("app:forbiddenItem")
    public static void setForbiddenItem(RecyclerView recyclerView, Server server) {
        ServersRecyclerViewAdapter adapter = (ServersRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setForbiddenServer(server);
        }
    }

    @BindingAdapter("app:servers")
    public static void setServerItems(RecyclerView recyclerView, List<Server> servers) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(servers);
        }
    }

    @BindingAdapter("app:excludedServers")
    public static void setExcludedItems(RecyclerView recyclerView, List<Server> excludedServers) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setExcludedServers(excludedServers);
        }
    }

    @BindingAdapter("app:emails")
    public static void setEmails(RecyclerView recyclerView, List<Email> emails) {
        PrivateEmailsRecyclerViewAdapter adapter = (PrivateEmailsRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setEmails(emails);
        }
    }

    @BindingAdapter("app:apps")
    public static void setApplicationsList(RecyclerView recyclerView, List<ApplicationItem> apps) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setApplicationsInfoList(apps);
        }
    }

    @BindingAdapter("app:not_allowed_apps")
    public static void setNotAllowedAppsList(RecyclerView recyclerView, List<String> notAllowedApps) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setDisallowedApps(notAllowedApps);
        }
    }

    @BindingAdapter("app:selection_listener")
    public static void setSelectionChangedListener(RecyclerView recyclerView, OnApplicationItemSelectionChangedListener listener) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setSelectionChangedListener(listener);
        }
    }

    @BindingAdapter("app:fastest_setting_listener")
    public static void setSelectionChangedListener(RecyclerView recyclerView,
                                                   OnFastestSettingChangedListener listener) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setSelectionChangedListener(listener);
        }
    }

    @BindingAdapter("app:wifi_list")
    public static void setWifiList(RecyclerView recyclerView, List<WifiItem> wifiList) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setWifiItemList(wifiList);
        }
    }

    @BindingAdapter("app:is_network_feature_enabled")
    public static void setIsUntrustedWifiEnabled(RecyclerView recyclerView, boolean isEnabled) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setNetworkRulesEnabled(isEnabled);
        }
    }

    @BindingAdapter("app:default_network_state")
    public static void setDefaultNetworkState(RecyclerView recyclerView, NetworkState defaultState) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setDefaultNetworkState(defaultState);
        }
    }

    @BindingAdapter("app:mobile_data_state")
    public static void setMobileDataNetworkState(RecyclerView recyclerView, NetworkState mobileDataState) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setMobileDataState(mobileDataState);
        }
    }

    @BindingAdapter("app:network_feature_listener")
    public static void setNetworkFeatureTouchListener(RecyclerView recyclerView,
                                                      OnNetworkFeatureStateChanged onNetworkFeatureStateChanged) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setOnNetworkFeatureStateChanged(onNetworkFeatureStateChanged);
        }
    }

    @BindingAdapter("android:layout_height")
    public static void setHeight(View view, boolean isMatchParent) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = isMatchParent ? MATCH_PARENT : WRAP_CONTENT;
        view.setLayoutParams(params);
    }

    @BindingAdapter("android:adapter")
    public static void setAdapter(RecyclerView view, RecyclerView.Adapter adapter) {
        view.setAdapter(adapter);
    }
}