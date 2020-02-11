package net.ivpn.client.ui.timepicker;

public interface OnDelayOptionSelected {
    void onDelayOptionSelected(PauseDelay pauseDelay);
    void onCancelAction();
    void onCustomDelaySelected(long delay);
}
