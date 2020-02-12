package net.ivpn.client.ui.serverlist.fastest;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import net.ivpn.client.R;
import net.ivpn.client.databinding.ViewFastestSettingServerItemBinding;
import net.ivpn.client.rest.data.model.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastestSettingViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SERVER_ITEM = 0;
    private static final int DESCRIPTION_ITEM = 1;

    private List<Server> servers = Collections.emptyList();
    private Set<Server> excludedServers = Collections.emptySet();

    private OnFastestSettingChangedListener listener;

    public FastestSettingViewAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return DESCRIPTION_ITEM;
        }
        return SERVER_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case SERVER_ITEM: {
                ViewFastestSettingServerItemBinding binding = ViewFastestSettingServerItemBinding.inflate(layoutInflater, parent, false);
                return new ServerViewHolder(binding);
            }
            default: {
                return new DescriptionViewHolder(layoutInflater.inflate(R.layout.view_fastest_setting_description_item, parent, false));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ServerViewHolder) {
            ((ServerViewHolder) holder).bind(getServerFor(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return servers.size() + 1;
    }

    public void replaceData(List<Server> servers) {
        setServers(new ArrayList<>(servers));
    }

    private void setServers(List<Server> servers) {
        Collections.sort(servers, Server.comparator);
        this.servers = servers;
        notifyDataSetChanged();
    }

    public void setExcludedServers(List<Server> excludedServers) {
        this.excludedServers = new HashSet<>(excludedServers);
    }

    public void setSelectionChangedListener(OnFastestSettingChangedListener listener) {
        this.listener = listener;
    }

    private Server getServerFor(int position) {
        return servers.get(position);
    }

    class ServerViewHolder extends RecyclerView.ViewHolder
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        private ViewFastestSettingServerItemBinding binding;
        private Server server;

        ServerViewHolder(ViewFastestSettingServerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Server server) {
            this.server = server;
            binding.setServer(server);
            boolean isChecked = !excludedServers.contains(server);
            binding.checkbox.setChecked(isChecked);
            binding.checkbox.setOnCheckedChangeListener(this);
            binding.checkbox.setClickable(false);
            binding.contentLayout.setOnClickListener(this);
            binding.executePendingBindings();
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
            if (isSelected) {
                excludedServers.remove(server);
            } else {
                excludedServers.add(server);
            }
            listener.onFastestSettingItemChanged(server, isSelected);
        }

        @Override
        public void onClick(View view) {
            boolean isExcluded = excludedServers.contains(server);
            boolean isLastServer = servers.size() - excludedServers.size() <= 1;
            if (isExcluded || !isLastServer) {
                binding.checkbox.setChecked(isExcluded);
            } else {
                listener.onAttemptRemoveLastServer();
            }
        }
    }

    class DescriptionViewHolder extends RecyclerView.ViewHolder {

        DescriptionViewHolder(View itemView) {
            super(itemView);
        }
    }
}