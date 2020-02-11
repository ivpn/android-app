package net.ivpn.client.ui.protocol.port;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.ivpn.client.R;

public class PortAdapter extends ArrayAdapter<Port> {
    private Context context;
    private int layoutResourceId;
    private Port[] ports;
    private int currentPosition;

    public PortAdapter(Context context, int layoutResourceId,
                       Port[] ports) {
        super(context, layoutResourceId, ports);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.ports = ports;
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

        Port item = ports[position];
        holder.portTextView.setText(item.toThumbnail());
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

        Port item = ports[position];
        holder.portTextView.setText(item.toThumbnail());
        return row;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public Port[] getAllowedPorts() {
        return ports;
    }

    static class Holder {
        TextView portTextView;
        View currentIcon;
    }
}