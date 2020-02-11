package com.todtenkopf.mvvm;

import android.databinding.Observable;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

public class MenuCommandBindings implements ViewModelActivity.IMenuCallbackListener {

    public enum EnableBinding {
        None,
        Visible,
        Enabled
    }

    private HashMap<Integer,MenuBinding> mCommandMap = new HashMap<>();
    private ViewModelActivity mActivity;

    public MenuCommandBindings(ViewModelActivity activity) {
        mActivity = activity;
        activity.setMenuCallbackListener(this);
    }

    public void addBinding(int menuId, Command cmd, EnableBinding enableBinding) {
        MenuBinding mb = new MenuBinding(menuId, cmd, enableBinding);
        mCommandMap.put(menuId, mb);
        cmd.addOnPropertyChangedCallback(mPropertyChangedCallback);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (Map.Entry<Integer,MenuBinding> entry : mCommandMap.entrySet()) {
            MenuItem item = menu.findItem(entry.getKey());
            if (item == null)
                continue; // items on popups can be bound for execute, but not for isEnabled (they are always visible)
            if (entry.getValue().mEnableBinding == EnableBinding.Enabled) {
                item.setEnabled(entry.getValue().mCommand.isEnabled());
            } else if (entry.getValue().mEnableBinding == EnableBinding.Visible) {
                item.setVisible(entry.getValue().mCommand.isEnabled());
            }
        }
    }

    @Override
    public void onOptionsItemSelected(int menuItemId) {
        MenuBinding mb = mCommandMap.get(menuItemId);
        if (mb != null) {
            mb.mCommand.execute();
        }
    }

    private final Observable.OnPropertyChangedCallback mPropertyChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            mActivity.invalidateOptionsMenu();
        }
    };

    private class MenuBinding {
        public int mMenuId;
        public Command mCommand;
        public EnableBinding mEnableBinding;

        public MenuBinding(int menuId, Command cmd, EnableBinding enableBinding) {
            mMenuId = menuId;
            mCommand = cmd;
            mEnableBinding = enableBinding;
        }
    }
}