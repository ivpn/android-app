package net.ivpn.client.common.bindings;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.ui.network.OnNetworkFeatureStateChanged;
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewAdapter;
import net.ivpn.client.ui.serverlist.fastest.OnFastestSettingChangedListener;
import net.ivpn.client.ui.split.OnApplicationItemSelectionChangedListener;
import net.ivpn.client.ui.split.SplitTunnelingRecyclerViewAdapter;
import net.ivpn.client.ui.split.data.ApplicationItem;
import net.ivpn.client.v2.network.NetworkRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.ServerBasedRecyclerViewAdapter;
import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.vpn.model.WifiItem;

import java.util.List;

import kotlin.Pair;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class RecyclerViewItemsBindingAdapter {

    @BindingAdapter("items")
    public static void setItems(RecyclerView recyclerView, List<Server> items) {
        ServerBasedRecyclerViewAdapter adapter = (ServerBasedRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("forbiddenItem")
    public static void setForbiddenItem(RecyclerView recyclerView, Server server) {
        ServerBasedRecyclerViewAdapter adapter = (ServerBasedRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setForbiddenServer(server);
        }
    }

    @BindingAdapter("servers")
    public static void setServerItems(RecyclerView recyclerView, List<Server> servers) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(servers);
        }
    }

    @BindingAdapter("excludedServers")
    public static void setExcludedItems(RecyclerView recyclerView, List<Server> excludedServers) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setExcludedServers(excludedServers);
        }
    }

    @BindingAdapter("apps")
    public static void setApplicationsList(RecyclerView recyclerView, List<ApplicationItem> apps) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setApplicationsInfoList(apps);
        }
    }

    @BindingAdapter("not_allowed_apps")
    public static void setNotAllowedAppsList(RecyclerView recyclerView, List<String> notAllowedApps) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setDisallowedApps(notAllowedApps);
        }
    }

    @BindingAdapter("selection_listener")
    public static void setSelectionChangedListener(RecyclerView recyclerView, OnApplicationItemSelectionChangedListener listener) {
        SplitTunnelingRecyclerViewAdapter adapter = (SplitTunnelingRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setSelectionChangedListener(listener);
        }
    }

    @BindingAdapter("fastest_setting_listener")
    public static void setSelectionChangedListener(RecyclerView recyclerView,
                                                   OnFastestSettingChangedListener listener) {
        FastestSettingViewAdapter adapter = (FastestSettingViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setSelectionChangedListener(listener);
        }
    }

    @BindingAdapter("wifi_list")
    public static void setWifiList(RecyclerView recyclerView, List<WifiItem> wifiList) {
        NetworkRecyclerViewAdapter adapter = (NetworkRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setWiFiList(wifiList);
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