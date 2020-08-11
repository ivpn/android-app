package net.ivpn.client.v2.network;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.databinding.ViewCommonNetworkBehaviourBinding;
import net.ivpn.client.databinding.ViewNetworkMainBinding;
import net.ivpn.client.databinding.ViewWifiItemBinding;
import net.ivpn.client.ui.network.CommonBehaviourItemViewModel;
import net.ivpn.client.ui.network.MobileDataItemViewModel;
import net.ivpn.client.ui.network.NetworkItemViewModel;
import net.ivpn.client.ui.network.OnNetworkFeatureStateChanged;
import net.ivpn.client.v2.dialog.DialogBuilderK;
import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.vpn.model.WifiItem;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import static net.ivpn.client.vpn.model.NetworkState.DEFAULT;
import static net.ivpn.client.vpn.model.NetworkState.NONE;

public class NetworkRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int WIFI_ITEM = 0;
    private static final int NETWORK_FEATURE_DESCRIPTION = 1;
    private static final int COMMON_ITEM = 2;

    private boolean isNetworkRulesEnabled;
    private NetworkState defaultState = NONE;
    private List<WifiItem> wifiItemList = new LinkedList<>();
    private NetworkState mobileDataState = DEFAULT;
    private OnNetworkFeatureStateChanged onNetworkFeatureStateChanged;

    private Context context;

    public NetworkRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0: {
                return NETWORK_FEATURE_DESCRIPTION;
            }
            case 1: {
                return COMMON_ITEM;
            }
            default: {
                return WIFI_ITEM;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isNetworkRulesEnabled) {
            return wifiItemList.size() + 2;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case WIFI_ITEM: {
                ViewWifiItemBinding binding = ViewWifiItemBinding.inflate(layoutInflater, parent, false);
                return new WifiItemViewHolder(binding);
            }
            case COMMON_ITEM: {
                ViewCommonNetworkBehaviourBinding binding = ViewCommonNetworkBehaviourBinding.inflate(layoutInflater, parent, false);
                return new CommonNetworkViewHolder(binding);
            }
            default: {
                ViewNetworkMainBinding binding = ViewNetworkMainBinding.inflate(layoutInflater, parent, false);
                return new NetworkFeatureViewHolder(binding);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WifiItemViewHolder) {
            ((WifiItemViewHolder) holder).bind(wifiItemList.get(position - 2));
        } else if (holder instanceof CommonNetworkViewHolder) {
            ((CommonNetworkViewHolder) holder).bind();
        } else if (holder instanceof NetworkFeatureViewHolder) {
            ((NetworkFeatureViewHolder) holder).bind();
        }
    }

    public void setWifiItemList(List<WifiItem> wifiItemList) {
        this.wifiItemList = wifiItemList;
    }

    public void setNetworkRulesEnabled(boolean isNetworkWatcherFeatureEnabled) {
        if (isNetworkRulesEnabled != isNetworkWatcherFeatureEnabled) {
            this.isNetworkRulesEnabled = isNetworkWatcherFeatureEnabled;
            notifyDataSetChanged();
        }
    }

    public void setDefaultNetworkState(NetworkState defaultState) {
        this.defaultState = defaultState;
    }

    public void setMobileDataState(NetworkState mobileDataState) {
        this.mobileDataState = mobileDataState;
    }

    private void updateUIWithDefaultValue(NetworkState defaultState) {
        setDefaultNetworkState(defaultState);
        notifyDataSetChanged();
    }

    public void setOnNetworkFeatureStateChanged(OnNetworkFeatureStateChanged onNetworkFeatureStateChanged) {
        this.onNetworkFeatureStateChanged = onNetworkFeatureStateChanged;
    }

    public class WifiItemViewHolder extends RecyclerView.ViewHolder {

        private ViewWifiItemBinding binding;
        @Inject
        NetworkItemViewModel viewModel;

        WifiItemViewHolder(ViewWifiItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);

            viewModel.setContext(binding.getRoot().getContext());
            viewModel.setDefaultState(defaultState);

            binding.setViewmodel(viewModel);
            binding.contentLayout.setOnClickListener(v -> DialogBuilderK.INSTANCE.openChangeNetworkStatusDialogue(context, viewModel));
        }

        public void bind(WifiItem wifiItem) {
            NetworkItemViewModel viewModel = binding.getViewmodel();
            viewModel.setWifiItem(wifiItem);
            viewModel.setDefaultState(defaultState);
            binding.executePendingBindings();
        }
    }

    class NetworkFeatureViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener {

        private ViewNetworkMainBinding binding;

        NetworkFeatureViewHolder(ViewNetworkMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            Log.d("NetworkFeature", "NetworkFeatureViewHolder: isNetworkRulesEnabled = " + isNetworkRulesEnabled);
            binding.wifiMainSwitcher.setChecked(isNetworkRulesEnabled);
            binding.setIsNetworkFilterEnabled(isNetworkRulesEnabled);
            binding.wifiMainSwitcher.setOnCheckedChangeListener(this);
            binding.rulesAction.setOnClickListener(v -> {
                onNetworkFeatureStateChanged.toRules();
            });
        }

        private void bind() {
            binding.wifiMainSwitcher.setChecked(isNetworkRulesEnabled);
            binding.setIsNetworkFilterEnabled(isNetworkRulesEnabled);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("NetworkFeature", "onCheckedChanged: isChecked = " + isChecked);
            if (isChecked == isNetworkRulesEnabled) {
                return;
            }
            isNetworkRulesEnabled = isChecked;
            binding.setIsNetworkFilterEnabled(isNetworkRulesEnabled);
            if (onNetworkFeatureStateChanged != null) {
                onNetworkFeatureStateChanged.onNetworkFeatureStateChanged(isChecked);
            }
            notifyDataSetChanged();
        }
    }

    public class CommonNetworkViewHolder extends RecyclerView.ViewHolder
            implements CommonBehaviourItemViewModel.OnDefaultBehaviourChanged {

        private ViewCommonNetworkBehaviourBinding binding;
        @Inject
        public CommonBehaviourItemViewModel defaultViewModel;
        @Inject
        public MobileDataItemViewModel mobileViewModel;

        CommonNetworkViewHolder(ViewCommonNetworkBehaviourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
            defaultViewModel.setNavigator(this);
            defaultViewModel.setContext(binding.getRoot().getContext());
            mobileViewModel.setContext(binding.getRoot().getContext());
            binding.setDefaultItem(defaultViewModel);
            binding.setMobileItem(mobileViewModel);

            binding.mobileContentLayout.setOnClickListener(v ->
                    DialogBuilderK.INSTANCE.openChangeNetworkStatusDialogue(context, mobileViewModel)
            );

            binding.defaultLayout.setOnClickListener(view ->
                    DialogBuilderK.INSTANCE.openChangeDefaultNetworkStatusDialogue(context, defaultViewModel)
            );
        }

        private void bind() {
            defaultViewModel.setDefaultState(defaultState);
            mobileViewModel.setDefaultState(defaultState);
            mobileViewModel.setCurrentState(mobileDataState);
            binding.executePendingBindings();
        }

        @Override
        public void onDefaultBehaviourChanged(NetworkState state) {
            if (defaultState == state) {
                return;
            }
            updateUIWithDefaultValue(state);
        }
    }
}