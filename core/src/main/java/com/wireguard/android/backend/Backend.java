/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import androidx.annotation.Nullable;

import com.wireguard.android.config.Config;
import com.wireguard.android.model.Tunnel;
import com.wireguard.android.model.Tunnel.State;

import java.util.Set;

/**
 * Interface for implementations of the WireGuard secure network tunnel.
 */

public interface Backend {

    /**
     * Get the state of a tunnel.
     *
     * @param tunnel The tunnel to examine the state of.
     * @return The state of the tunnel.
     * @throws Exception Exception raised when retrieving tunnel's state.
     */
    Tunnel.State getState(Tunnel tunnel) throws Exception;

    /**
     * Determine version of underlying backend.
     *
     * @return The version of the backend.
     * @throws Exception Exception raised while retrieving version.
     */
    String getVersion() throws Exception;

    /**
     * Set the state of a tunnel, updating it's configuration. If the tunnel is already up, config
     * may update the running configuration; config may be null when setting the tunnel down.
     *
     * @param tunnel The tunnel to control the state of.
     * @param state  The new state for this tunnel. Must be {@code UP}, {@code DOWN}, or
     *               {@code TOGGLE}.
     * @param config The configuration for this tunnel, may be null if state is {@code DOWN}.
     * @return The updated state of the tunnel.
     * @throws Exception Exception raised while changing state.
     */
    Tunnel.State setState(Tunnel tunnel, Tunnel.State state, @Nullable Config config) throws Exception;
}
