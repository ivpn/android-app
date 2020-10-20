package net.ivpn.client.ui.timepicker;

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

import net.ivpn.client.R;

public enum PauseDelay {
    FIVE_MINUTES(5 * PauseDelay.MINUTE, R.string.dialogs_five_minutes),
    FIFTEEN_MINUTES(15 * PauseDelay.MINUTE, R.string.dialogs_fifteen_minutes),
    ONE_HOUR(PauseDelay.HOUR, R.string.dialogs_one_hour),
    CUSTOM_DELAY(0, R.string.dialogs_custom_delay);

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;

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
