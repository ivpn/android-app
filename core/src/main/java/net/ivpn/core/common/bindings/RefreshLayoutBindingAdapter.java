package net.ivpn.core.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.ivpn.core.v2.viewmodel.ServerListViewModel;

public class RefreshLayoutBindingAdapter {

    /**
     * Reloads the data when the pull-to-refresh is triggered.
     *
     * Creates the {@code android:onRefresh} for a {@link SwipeRefreshLayout}.
     */
    @BindingAdapter("android:onRefresh")
    public static void setSwipeRefreshLayoutOnRefreshListener(SwipeRefreshLayout view,
                                                              final ServerListViewModel viewModel) {
        view.setOnRefreshListener(() -> viewModel.loadServers(true));
    }
}