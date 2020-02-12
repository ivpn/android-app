package net.ivpn.client.ui.network;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.ivpn.client.R;
import net.ivpn.client.vpn.model.NetworkState;

public class NetworkAdapter extends ArrayAdapter<NetworkState> {
    private static final String TAG = NetworkAdapter.class.getSimpleName();

    private Context context;
    private NetworkState[] states;
    private int currentPosition;
    private NetworkState defaultState;

    public NetworkAdapter(Context context, NetworkState[] states, NetworkState defaultState) {
        super(context, R.layout.view_network_behaviour, states);
        Log.d(TAG, "NetworkAdapter: states.length = " + states.length);
        this.context = context;
        this.states = states;
        this.defaultState = defaultState;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            // at this point we inflate the view with our custom layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.view_network_behaviour, parent, false);

            holder = new Holder();
            holder.networkBehavior = row.findViewById(R.id.network_behaviour);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        NetworkState item = states[position];
        holder.networkBehavior.setText(item.getTextRes());
        return row;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        Holder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.view_network_behaviour_selected, parent, false);

            holder = new Holder();
            holder.networkBehavior = row.findViewById(R.id.network_behaviour);
            holder.networkBehaviorExtra = row.findViewById(R.id.network_behaviour_extra);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        NetworkState state = states[position];
        holder.networkBehavior.setText(state.getTextRes());
        holder.networkBehavior.setTextColor(context.getResources().getColor(state.getTextColor()));
        if (state.equals(NetworkState.DEFAULT)) {
            holder.networkBehaviorExtra.setVisibility(View.VISIBLE);
            holder.networkBehaviorExtra.setText(defaultState.getTextRes());
        } else {
            holder.networkBehaviorExtra.setVisibility(View.GONE);
        }
        currentPosition = position;
        return row;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public void setDefaultState(NetworkState defaultState) {
        if (this.defaultState == defaultState) {
            return;
        }
        this.defaultState = defaultState;
        if (isCurrentStateDefault()) {
            notifyDataSetChanged();
        }
    }

    public NetworkState[] getAllowedStates() {
        return states;
    }

    private boolean isCurrentStateDefault() {
        return states[currentPosition].equals(NetworkState.DEFAULT);
    }

    static class Holder {
        TextView networkBehavior;
        TextView networkBehaviorExtra;
    }
}