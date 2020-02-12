package com.todtenkopf.mvvm;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

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
