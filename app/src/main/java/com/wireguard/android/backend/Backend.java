/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import com.wireguard.android.config.Config;
import com.wireguard.android.model.Tunnel;
import com.wireguard.android.model.Tunnel.State;

import java.util.Set;

/**
 * Interface for implementations of the WireGuard secure network tunnel.
 */

public interface Backend {

    /**
     * Get the actual state of a tunnel.
     *
     * @param tunnel The tunnel to examine the state of.
     * @return The state of the tunnel.
     */
    State getState(Tunnel tunnel) throws Exception;

    /**
     * Set the state of a tunnel.
     *
     * @param tunnel The tunnel to control the state of.
     * @param state  The new state for this tunnel. Must be {@code UP}, {@code DOWN}, or
     *               {@code TOGGLE}.
     * @return The updated state of the tunnel.
     */
    State setState(Tunnel tunnel, State state) throws Exception;

    /**
     * Determine version of underlying backend.
     *
     * @return The version of the backend.
     * @throws Exception
     */
    String getVersion() throws Exception;
}
