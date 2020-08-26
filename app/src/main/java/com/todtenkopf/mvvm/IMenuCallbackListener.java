package com.todtenkopf.mvvm;

import android.view.Menu;

public interface IMenuCallbackListener {
    void onPrepareOptionsMenu(Menu menu);
    void onOptionsItemSelected(int menuItemId);
}
