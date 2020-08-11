package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.ivpn.client.v2.viewmodel.ServerListViewModel;


public class RefreshLayoutBindingAdapter {

    /**
     * Reloads the data when the pull-to-refresh is triggered.
     * <p>
     * Creates the {@code android:onRefresh} for a {@link SwipeRefreshLayout}.
     */
    @BindingAdapter("android:onRefresh")
    public static void setSwipeRefreshLayoutOnRefreshListener(SwipeRefreshLayout view,
                                                              final ServerListViewModel viewModel) {
        view.setOnRefreshListener(() -> viewModel.loadServers(true));
    }
}