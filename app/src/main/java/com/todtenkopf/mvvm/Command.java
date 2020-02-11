package com.todtenkopf.mvvm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public abstract class Command extends BaseObservable
{
    private boolean mIsEnabled;
    @Bindable
    public boolean isEnabled() {
        return mIsEnabled;
    }
    public void isEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
        notifyChange();
    }

    public abstract void execute();

}
