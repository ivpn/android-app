package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;

import net.ivpn.client.R;
import net.ivpn.client.ui.connect.ConnectionState;

public class SwitchButtonBindingAdapter {

    @BindingAdapter("onChanged")
    public static void setOnChangedSwitchButtonListener(SwitchCompat view,
                                                        OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("onTouch")
    public static void setOnTouchListener(SwitchCompat view,
                                          final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter("onTouch")
    public static void setOnTouchListener(View view,
                                          final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter("connectionState")
    public static void setConnectionState(SwitchCompat switchView, ConnectionState state) {
        int thumbRes = 0;
        int trackRes = 0;

        switch (state) {
            case NOT_CONNECTED:
                thumbRes = R.drawable.thumb_disconnected;
                trackRes = R.drawable.track_disconnected;
                break;
            case CONNECTING:
                thumbRes = R.drawable.thumb_connecting;
                trackRes = R.drawable.track_connecting;
                break;
            case CONNECTED:
                thumbRes = R.drawable.thumb_connected;
                trackRes = R.drawable.track_connected;
                break;
            case DISCONNECTING:
                thumbRes = R.drawable.thumb_disconnecting;
                trackRes = R.drawable.track_disconnecting;
                break;
            case PAUSING:
            case PAUSED:
                thumbRes = R.drawable.thumb_paused;
                trackRes = R.drawable.track_paused;
                break;
        }

        switchView.setThumbResource(thumbRes);
        switchView.setTrackResource(trackRes);
    }
}