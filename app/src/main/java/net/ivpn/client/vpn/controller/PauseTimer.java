package net.ivpn.client.vpn.controller;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.os.CountDownTimer;

import net.ivpn.client.ui.connect.ConnectionState;

import org.jetbrains.annotations.Nullable;

class PauseTimer {
    private static final long TICK = 1000L;

    private CountDownTimer timer;
    private PauseTimerListener listener;
    private long millisUntilFinished;

    PauseTimer(PauseTimerListener listener) {
        this.listener = listener;
    }

    VpnStateListener getListener() {
        return new VpnStateListenerImpl() {
            @Override
            public void onConnectionStateChanged(@Nullable ConnectionState state) {
                super.onConnectionStateChanged(state);
            }
        };
    }

    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    void startTimer(long pauseDuration) {
        if (timer != null) {
            timer.cancel();
        }
        timer = getTimer(pauseDuration);
        timer.start();
    }

    long getMillisUntilFinished() {
        return millisUntilFinished;
    }

    private CountDownTimer getTimer(long pauseTime) {
        return new CountDownTimer(pauseTime, TICK) {
            @Override
            public void onTick(long millisUntilFinished) {
                PauseTimer.this.millisUntilFinished = millisUntilFinished;
                listener.onTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                millisUntilFinished = 0;
                listener.onFinish();
            }
        };
    }

    public interface PauseTimerListener {
        void onTick(long millisUntilFinished);
        void onFinish();
    }
}