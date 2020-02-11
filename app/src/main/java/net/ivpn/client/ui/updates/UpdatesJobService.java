package net.ivpn.client.ui.updates;

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
