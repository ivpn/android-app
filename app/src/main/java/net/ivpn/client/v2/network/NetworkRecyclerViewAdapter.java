package net.ivpn.client.v2.network;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.databinding.ViewWifiItemBinding;
import net.ivpn.client.v2.dialog.DialogBuilderK;
import net.ivpn.client.v2.network.dialog.NetworkChangeDialogViewModel;
import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.vpn.model.WifiItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import static net.ivpn.client.vpn.model.NetworkState.NONE;

public class NetworkRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Logger LOGGER = LoggerFactory.getLogger(NetworkViewModel.class);

    private static final int WIFI_ITEM = 0;

    private NetworkState defaultState = NONE;
    private List<WifiItem> wifiItemList = new LinkedList<>();
    private NetworkStateFormatter formatter;

    @Inject public NetworkViewModel network;

    public NetworkRecyclerViewAdapter(Context context) {
        formatter = new NetworkStateFormatter(context);
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
    }

    @Override
    public int getItemViewType(int position) {
        return WIFI_ITEM;
    }

    @Override
    public int getItemCount() {
        return wifiItemList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewWifiItemBinding binding = ViewWifiItemBinding.inflate(layoutInflater, parent, false);
        return new WifiItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WifiItemViewHolder) {
            ((WifiItemViewHolder) holder).bind(wifiItemList.get(position));
        }
    }

    public void setWiFiList(List<WifiItem> wifiItemList) {
        this.wifiItemList = wifiItemList;
        notifyDataSetChanged();
    }
    

    public class WifiItemViewHolder extends RecyclerView.ViewHolder {

        private ViewWifiItemBinding binding;

        private WifiItem item;

        WifiItemViewHolder(ViewWifiItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.setViewmodel(network);
            binding.setFormatter(formatter);
            binding.contentLayout.setOnClickListener(v ->
                    DialogBuilderK.INSTANCE.openChangeNetworkStatusDialogue(binding.getRoot().getContext(),
                    new NetworkChangeDialogViewModel(item.getNetworkState().get()) {
                @Override
                public void apply() {
                    network.setWifiStateAs(item, getSelectedState().get());
                    item.getNetworkState().set(getSelectedState().get());
                }
            }));
        }

        public void bind(WifiItem wifiItem) {
            LOGGER.info("Bind Wifi item = " + wifiItem + " Default state = " + defaultState);
            this.item = wifiItem;
            binding.setWifi(wifiItem);
        }
    }
}