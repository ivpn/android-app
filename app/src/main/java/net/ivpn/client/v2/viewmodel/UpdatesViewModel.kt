package net.ivpn.client.v2.viewmodel

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

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.IVPNApplication
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.updater.OnUpdateCheckListener
import net.ivpn.client.common.updater.Update
import net.ivpn.client.common.updater.UpdateHelper
import net.ivpn.client.ui.updates.UpdatesJobServiceUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class UpdatesViewModel @Inject constructor(
        private val buildController: BuildController,
        private val updateHelper: UpdateHelper,
        private val updatesJobServiceUtil: UpdatesJobServiceUtil,
        private val settings: Settings
) : ViewModel() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UpdatesViewModel::class.java)
    }

    val isUpdatesSupported = ObservableBoolean()
    val isAutoUpdateEnabled = ObservableBoolean()
    val currentVersion = ObservableField<String>()
    val nextVersion = ObservableField<String>()
    val isUpToDate = ObservableBoolean()
    val isNextVersionAvailable = ObservableBoolean()
    val isInProgress = ObservableBoolean()
    var enableAutoUpdates = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAutoUpdates(value) }
    private val listener: OnUpdateCheckListener = getUpdateCheckListener()

    init {
        updateHelper.subscribe(listener)
        currentVersion.set(updateHelper.getCurrentVersion())
        isAutoUpdateEnabled.set(getAutoUpdateValue())
        updateHelper.notifyAboutAvailableVersion()
    }

    fun onResume() {
        isUpdatesSupported.set(getUpdateSupport())
    }

    fun reset() {
        isUpdatesSupported.set(getUpdateSupport())
    }

    private fun getUpdateSupport(): Boolean {
        return buildController.isUpdatesSupported
    }

    fun checkForUpdates() {
        LOGGER.info("Check for updates")
        isInProgress.set(true)
        updateHelper.checkForUpdates()
    }

    fun proceed() {
        LOGGER.info("proceed")
        updateHelper.proceedUpdate()
    }

    fun release() {
        LOGGER.info("release")
        updateHelper.unsubscribe(listener)
    }

    private fun enableAutoUpdates(isEnabled: Boolean) {
        LOGGER.info("Is auto update enabled? $isEnabled")
        if (isAutoUpdateEnabled.get() == isEnabled) {
            return
        }

        settings.isAutoUpdateEnabled = isEnabled
        isAutoUpdateEnabled.set(isEnabled)
        if (isEnabled) {
            updatesJobServiceUtil.pushUpdateJob(IVPNApplication.getApplication())
        } else {
            updatesJobServiceUtil.clearUpdateJob(IVPNApplication.getApplication())
        }
    }

    private fun getAutoUpdateValue(): Boolean {
        return settings.isAutoUpdateEnabled
    }

    private fun getUpdateCheckListener(): OnUpdateCheckListener {
        return object : OnUpdateCheckListener {
            override fun onError(exception: Exception) {
                LOGGER.error("Error getting information about update: ", exception)
                isInProgress.set(false)
                isUpToDate.set(false)
                isNextVersionAvailable.set(false)
            }

            override fun onUpdateAvailable(update: Update) {
                LOGGER.info("Update is available: ", update.latestVersion)
                isInProgress.set(false)
                isUpToDate.set(false)
                nextVersion.set(update.latestVersion)
                isNextVersionAvailable.set(true)
            }

            override fun onVersionUpToDate() {
                LOGGER.info("Current version is up to date")
                isInProgress.set(false)
                isUpToDate.set(true)
                isNextVersionAvailable.set(false)
            }
        }
    }
}