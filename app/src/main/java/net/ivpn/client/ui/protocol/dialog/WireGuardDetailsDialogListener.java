package net.ivpn.client.ui.protocol.dialog;

public interface WireGuardDetailsDialogListener {

    void reGenerateKeys();

    void copyPublicKeyToClipboard();

    void copyIpAddressToClipboard();
}
