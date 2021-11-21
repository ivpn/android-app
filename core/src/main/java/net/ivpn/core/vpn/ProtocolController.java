package net.ivpn.core.vpn;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.prefs.StickyPreference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class ProtocolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolController.class);
    private Protocol currentProtocol;
    private List<OnProtocolChangedListener> listeners;

    private StickyPreference stickyPreference;

    @Inject
    public ProtocolController(StickyPreference stickyPreference) {
        this.stickyPreference = stickyPreference;

        listeners = new ArrayList<>();
        currentProtocol = stickyPreference.getCurrentProtocol();
    }

    public void init() {
        LOGGER.info("Init protocol controller");
        currentProtocol = stickyPreference.getCurrentProtocol();
        notify(currentProtocol);
    }

    public void reset() {
        init();
    }

    public Protocol getCurrentProtocol() {
        return currentProtocol;
    }

    public void setCurrentProtocol(Protocol currentProtocol) {
        if (this.currentProtocol == currentProtocol) {
            return;
        }
        LOGGER.info("setCurrentProtocol currentProtocol = " + currentProtocol);
        this.currentProtocol = currentProtocol;
        notify(currentProtocol);
        stickyPreference.setCurrentProtocol(currentProtocol);
    }

    public boolean isProtocolSelected() {
        return stickyPreference.isProtocolSelected();
    }

    public void addOnProtocolChangedListener(OnProtocolChangedListener listener) {
        listeners.add(listener);
        notify(currentProtocol);
    }

    public void removeOnProtocolChangedListener(OnProtocolChangedListener listener) {
        listeners.remove(listener);
    }

    private void notify(Protocol protocol) {
        for (OnProtocolChangedListener listener: listeners) {
            listener.onProtocolChanged(protocol);
        }
    }
}