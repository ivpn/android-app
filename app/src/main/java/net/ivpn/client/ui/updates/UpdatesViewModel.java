package net.ivpn.client.ui.updates;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import android.widget.CompoundButton;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.updater.OnUpdateCheckListener;
import net.ivpn.client.common.updater.Update;
import net.ivpn.client.common.updater.UpdateHelper;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UpdatesViewModel {

//    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesViewModel.class);
//
//    public final ObservableBoolean isAutoUpdateEnabled = new ObservableBoolean();
//    public final ObservableField<String> currentVersion = new ObservableField<>();
//    public final ObservableField<String> nextVersion = new ObservableField<>();
//    public final ObservableBoolean isUpToDate = new ObservableBoolean();
//    public final ObservableBoolean isNextVersionAvailable = new ObservableBoolean();
//    public final ObservableBoolean isInProgress = new ObservableBoolean();
//    public CompoundButton.OnCheckedChangeListener enableAutoUpdates = (compoundButton, value) -> enableAutoUpdates(value);
//    private OnUpdateCheckListener listener = getUpdateCheckListener();
//
//    private Settings settings;
//    private UpdateHelper updateHelper;
//    private UpdatesJobServiceUtil updatesJobServiceUtil;
//
//    @Inject
//    public UpdatesViewModel(UpdateHelper updateHelper, UpdatesJobServiceUtil updatesJobServiceUtil, Settings settings) {
//        this.settings = settings;
//        this.updateHelper = updateHelper;
//        this.updatesJobServiceUtil = updatesJobServiceUtil;
//        init();
//    }
//
//    private void init() {
//        updateHelper.subscribe(listener);
//        currentVersion.set(updateHelper.getCurrentVersion());
//        isAutoUpdateEnabled.set(isAutoUpdateEnabled());
//        updateHelper.notifyAboutAvailableVersion();
//    }
//
//    public void checkForUpdates() {
//        LOGGER.info("Check for updates");
//        isInProgress.set(true);
//        updateHelper.checkForUpdates();
//    }
//
//    public void proceed() {
//        LOGGER.info("proceed");
//        updateHelper.proceedUpdate();
//    }
//
//    public void release() {
//        LOGGER.info("release");
//        updateHelper.unsubscribe(listener);
//    }
//
//    private void enableAutoUpdates(boolean isEnabled) {
//        LOGGER.info("Is auto update enabled? " + isEnabled);
//        settings.enableAutoUpdate(isEnabled);
//        isAutoUpdateEnabled.set(isEnabled);
//        if (isEnabled) {
//            updatesJobServiceUtil.pushUpdateJob(IVPNApplication.getApplication());
//        } else {
//            updatesJobServiceUtil.clearUpdateJob(IVPNApplication.getApplication());
//        }
//    }
//
//    private boolean isAutoUpdateEnabled() {
//        return settings.isAutoUpdateEnabled();
//    }
//
//    private OnUpdateCheckListener getUpdateCheckListener() {
//        return new OnUpdateCheckListener() {
//            @Override
//            public void onError(@NotNull Exception exception) {
//                LOGGER.error("Error getting information about update: ", exception);
//                isInProgress.set(false);
//                isUpToDate.set(false);
//                isNextVersionAvailable.set(false);
//            }
//
//            @Override
//            public void onUpdateAvailable(@NotNull Update update) {
//                LOGGER.info("Update is available: ", update.getLatestVersion());
//                isInProgress.set(false);
//                isUpToDate.set(false);
//                nextVersion.set(update.getLatestVersion());
//                isNextVersionAvailable.set(true);
//            }
//
//            @Override
//            public void onVersionUpToDate() {
//                LOGGER.info("Current version is up to date");
//                isInProgress.set(false);
//                isUpToDate.set(true);
//                isNextVersionAvailable.set(false);
//            }
//        };
//    }
}