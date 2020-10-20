package net.ivpn.client.ui.updates;

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

import android.app.job.JobParameters;
import android.app.job.JobService;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.updater.OnUpdateCheckListener;
import net.ivpn.client.common.updater.Update;
import net.ivpn.client.common.updater.UpdateHelper;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UpdatesJobService extends JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesJobService.class);

    @Inject
    UpdateHelper updateHelper;

    private JobParameters jobParameters;
    private OnUpdateCheckListener listener = new OnUpdateCheckListener() {
        @Override
        public void onUpdateAvailable(@NotNull Update update) {
            LOGGER.info("New update is available");
            finishUpdatesCheck();
        }

        @Override
        public void onVersionUpToDate() {
            LOGGER.info("Current version is up to date");
            finishUpdatesCheck();
        }

        @Override
        public void onError(@NotNull Exception exception) {
            LOGGER.error("Got error while searching the newest version");
            finishUpdatesCheck();
        }
    };

    @Override
    public void onCreate() {
        IVPNApplication.getApplication().appComponent.inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        LOGGER.info("On start job...");
        this.jobParameters = jobParameters;
        updateHelper.subscribe(listener);
        updateHelper.checkForUpdates();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        LOGGER.error("On stop job...");
        return false;
    }

    private void finishUpdatesCheck() {
        updateHelper.unsubscribe(listener);
        jobFinished(jobParameters, false);
    }
}
