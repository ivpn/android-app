package com.todtenkopf.mvvm;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public abstract class ViewModelBase extends ViewModel {

    protected ArrayList<CommandVM> mCommands;

    protected void refreshCommands() {
        for (CommandVM cmd : mCommands)
            cmd.refresh();
    }

    public abstract class CommandVM extends Command {
        public void refresh() {
        }

        public CommandVM() {
            if (mCommands == null) {
                mCommands = new ArrayList<>();
            }
            mCommands.add(this);
        }
    }
}