package net.ivpn.client.ui.updates;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.utils.DateUtil;

import javax.inject.Inject;

@ApplicationScope
public class UpdatesJobServiceUtil {

    private static final int JOB_ID = 721;

    private Settings settings;

    @Inject
    public UpdatesJobServiceUtil(Settings settings) {
        this.settings = settings;
    }

    public void pushUpdateJob(Context context) {

        if (!isUpdatesEnabled()) return;
        boolean isEnabled = settings.isAutoUpdateEnabled();
        if (!isEnabled) return;

        ComponentName name = new ComponentName(context, UpdatesJobService.class);
        JobInfo info = new JobInfo.Builder(JOB_ID, name)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(DateUtil.HOUR * 12)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(info);
    }

    public void clearUpdateJob(Context context) {
        if (!isUpdatesEnabled()) return;

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    private boolean isUpdatesEnabled () {
        return BuildConfig.BUILD_VARIANT.equals("site");
    }
}
