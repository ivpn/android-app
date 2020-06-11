package net.ivpn.client.vpn;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.StickyPreference;

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
    }

    public void init() {
        LOGGER.info("Init protocol controller");
        currentProtocol = Protocol.valueOf(stickyPreference.getCurrentProtocol());
        notify(currentProtocol);
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
        stickyPreference.putCurrentProtocol(currentProtocol);
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