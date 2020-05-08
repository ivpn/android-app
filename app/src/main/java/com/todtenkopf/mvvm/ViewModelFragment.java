package com.todtenkopf.mvvm;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class ViewModelFragment extends Fragment {
    protected ViewModelBase mViewModel;
    private MenuCommandBindings mMenuBindings;
    private IMenuCallbackListener mMenuCallbackListener;

    public void setMenuCallbackListener(IMenuCallbackListener listener) {
        mMenuCallbackListener = listener;
    }

    @Nullable
    protected abstract ViewModelBase createViewModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = createViewModel();
        setHasOptionsMenu(true);
    }

    // subclasses should call super for onPrepareOptionsMenu and onOptionsItemSelected
    // FIRST, ignore the return value, then do their processing and return true
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mMenuCallbackListener != null) {
            mMenuCallbackListener.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMenuCallbackListener != null) {
            mMenuCallbackListener.onOptionsItemSelected(item.getItemId());
        }
        return false;
    }

    protected void addMenuBinding(int menuId, ViewModelBase.CommandVM cmd, MenuCommandBindings.EnableBinding enableBinding) {
        if (mMenuBindings == null) {
            mMenuBindings = new MenuCommandBindings(this);
        }
        mMenuBindings.addBinding(menuId, cmd, enableBinding);
    }
}
