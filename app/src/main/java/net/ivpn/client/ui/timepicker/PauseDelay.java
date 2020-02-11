package net.ivpn.client.ui.timepicker;

import net.ivpn.client.R;

public enum PauseDelay {
    FIVE_MINUTES(5 * PauseDelay.MINUTE, R.string.dialogs_five_minutes),
    FIFTEEN_MINUTES(15 * PauseDelay.MINUTE, R.string.dialogs_fifteen_minutes),
    ONE_HOUR(PauseDelay.HOUR, R.string.dialogs_one_hour),
    CUSTOM_DELAY(0, R.string.dialogs_custom_delay);

    private static final long MINUTE = 60 * 1000;
    private static final long HOUR = 60 * 60 * 1000;

    private long delay;
    private int labelId;

    PauseDelay(long delay, int labelId) {
        this.delay = delay;
        this.labelId = labelId;
    }

    public int getLabelId() {
        return labelId;
    }

    public long getDelay() {
        return delay;
    }
}
