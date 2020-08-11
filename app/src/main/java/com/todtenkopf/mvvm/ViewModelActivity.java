package com.todtenkopf.mvvm;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public abstract class ViewModelActivity extends AppCompatActivity {

    protected ViewModelBase mViewModel;
    private MenuCommandBindings mMenuBindings;
    private IMenuCallbackListener mMenuCallbackListner;

    public void setMenuCallbackListener(IMenuCallbackListener listener) {
        mMenuCallbackListner = listener;
    }

    @Nullable
    protected abstract ViewModelBase createViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = createViewModel();
    }

    // subclasses should call super for onPrepareOptionsMenu and onOptionsItemSelected
    // FIRST, ignore the return value, then do their processing and return true
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMenuCallbackListner != null) {
            mMenuCallbackListner.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMenuCallbackListner != null) {
            mMenuCallbackListner.onOptionsItemSelected(item.getItemId());
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