package net.ivpn.client.ui.split;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import net.ivpn.client.R;
import net.ivpn.client.databinding.ApplicationItemBinding;
import net.ivpn.client.ui.split.data.ApplicationItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class SplitTunnelingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int APP_ITEM = 0;
    private static final int DESCRIPTION_ITEM = 1;

    private List<ApplicationItem> allApps = new LinkedList<>();
    private Set<String> disallowedApps = new HashSet<>();
    private List<ApplicationItemBinding> bindings = new LinkedList<>();
    private OnApplicationItemSelectionChangedListener listener;

    @Inject
    SplitTunnelingRecyclerViewAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return DESCRIPTION_ITEM;
        }
        return APP_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case APP_ITEM: {
                ApplicationItemBinding binding = ApplicationItemBinding.inflate(layoutInflater, parent, false);
                bindings.add(binding);
                return new ApplicationInfoViewHolder(binding);
            }
            default: {
                return new DescriptionViewHolder(layoutInflater.inflate(R.layout.description_item, parent, false));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ApplicationInfoViewHolder) {
            ((ApplicationInfoViewHolder) holder).bind(allApps.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return allApps.size() + 1;
    }

    public void setSelectionChangedListener(OnApplicationItemSelectionChangedListener listener) {
        this.listener = listener;
    }

    MenuHandler getMenuHandler() {
        return new MenuHandler();
    }

    public void setApplicationsInfoList(List<ApplicationItem> infoList) {
        this.allApps = new ArrayList<>(infoList);
        Collections.sort(allApps, ApplicationItem.comparator);
        notifyDataSetChanged();
    }

    public void setDisallowedApps(List<String> disallowedApps) {
        this.disallowedApps = new HashSet<>(disallowedApps);
        if (listener != null) {
            listener.onItemsSelectionStateChanged(disallowedApps.size() == 0);
        }
    }

    private void selectAll() {
        disallowedApps = new HashSet<>();
        listener.onItemsSelectionStateChanged(true);
        //Better alternative than notifyItemRangeChanged for our case;
        setSelections(true);
    }

    private void deselectAll() {
        disallowedApps = new HashSet<>();
        for (ApplicationItem app : allApps) {
            disallowedApps.add(app.getPackageName());
        }
        listener.onItemsSelectionStateChanged(false);
        //Better alternative than notifyItemRangeChanged for our case;
        setSelections(false);
    }

    private void setSelections(boolean isSelected) {
        for (ApplicationItemBinding binding : bindings) {
            binding.checkbox.setChecked(isSelected);
        }
    }

    class ApplicationInfoViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        private ApplicationItemBinding binding;
        private ApplicationItem applicationItem;

        ApplicationInfoViewHolder(ApplicationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ApplicationItem applicationItem) {
            this.applicationItem = applicationItem;
            binding.setApplication(applicationItem);
            boolean isChecked = !disallowedApps.contains(applicationItem.getPackageName());
            binding.checkbox.setChecked(isChecked);
            binding.checkbox.setOnCheckedChangeListener(this);
            binding.contentLayout.setOnClickListener(this);
            binding.executePendingBindings();
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
            if (isSelected) {
                disallowedApps.remove(applicationItem.getPackageName());
            } else {
                disallowedApps.add(applicationItem.getPackageName());
            }
            listener.onItemsSelectionStateChanged(disallowedApps.size() == 0);
            listener.onApplicationItemSelectionChanged(applicationItem, isSelected);
        }

        @Override
        public void onClick(View view) {
            boolean isNotAllowed = disallowedApps.contains(applicationItem.getPackageName());
            binding.checkbox.setChecked(isNotAllowed);
        }
    }

    class DescriptionViewHolder extends RecyclerView.ViewHolder {

        DescriptionViewHolder(View itemView) {
            super(itemView);
        }
    }

    class MenuHandler {
        void selectAll() {
            SplitTunnelingRecyclerViewAdapter.this.selectAll();
        }

        void deselectAll() {
            SplitTunnelingRecyclerViewAdapter.this.deselectAll();
        }
    }
}