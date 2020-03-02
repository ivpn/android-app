/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.model;

import androidx.annotation.Nullable;

import com.wireguard.android.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ivpn.client.IVPNApplication;

public class Tunnel {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tunnel.class);

    private Config config;
    private String name;
    private State state;

    public Tunnel(final String name, final Config config, final State state) {
        this.name = name;
        this.config = config;
        this.state = state;
    }

    @Nullable
    public Config getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(final State state) {
        try {
            this.state = state;
            IVPNApplication.getApplication().appComponent.provideGoBackend().setState(this, state);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    public enum State {
        DOWN,
        TOGGLE,
        UP;

        public static State of(final boolean running) {
            return running ? UP : DOWN;
        }
    }
}