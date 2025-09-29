package net.ivpn.core.v2.protocol.port;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.ivpn.core.common.multihop.MultiHopController;

import net.ivpn.core.R;
import net.ivpn.core.rest.data.model.Port;
import net.ivpn.core.vpn.model.ObfuscationType;

import java.util.List;

public class PortAdapter extends ArrayAdapter<Port> {
    private Context context;
    private int layoutResourceId;
    private List<Port> ports;
    private int currentPosition;
    private MultiHopController multiHopController;
    private ObfuscationType obfuscationType;

    public PortAdapter(Context context, int layoutResourceId,
                       List<Port> ports, MultiHopController multiHopController) {
        super(context, layoutResourceId, ports);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.ports = ports;
        this.multiHopController = multiHopController;
        this.obfuscationType = null;
    }

    public PortAdapter(Context context, int layoutResourceId,
                       List<Port> ports, MultiHopController multiHopController, ObfuscationType obfuscationType) {
        super(context, layoutResourceId, ports);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.ports = ports;
        this.multiHopController = multiHopController;
        this.obfuscationType = obfuscationType;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            // at this point we inflate the view with our custom layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.port_item_list, parent, false);

            holder = new Holder();
            holder.portTextView = row.findViewById(R.id.port_description);
            holder.currentIcon = row.findViewById(R.id.port_current);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        if (position == currentPosition) {
            holder.currentIcon.setVisibility(View.VISIBLE);
        } else {
            holder.currentIcon.setVisibility(View.GONE);
        }

        Port item = ports.get(position);
        if (multiHopController.isEnabled()) {
            holder.portTextView.setText(item.getProtocol());
        } else {
            holder.portTextView.setText(item.toThumbnailWithObfuscation(obfuscationType));
        }

        return row;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.portTextView = row.findViewById(R.id.port_description);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        Port item = ports.get(position);
        if (multiHopController.isEnabled()) {
            holder.portTextView.setText(item.getProtocol());
        } else {
            holder.portTextView.setText(item.toThumbnailWithObfuscation(obfuscationType));
        }

        return row;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public List<Port> getAllowedPorts() {
        return ports;
    }

    static class Holder {
        TextView portTextView;
        View currentIcon;
    }
}