package net.ivpn.client.v2.updates

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

import android.app.job.JobParameters
import android.app.job.JobService
import net.ivpn.client.IVPNApplication
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.updater.OnUpdateCheckListener
import net.ivpn.client.common.updater.Update
import net.ivpn.client.common.updater.UpdateHelper
import org.slf4j.LoggerFactory
import javax.inject.Inject

@ApplicationScope
class UpdatesJobService @Inject constructor(): JobService() {
    @Inject
    lateinit var updateHelper: UpdateHelper
    private var jobParameters: JobParameters? = null

    private val listener: OnUpdateCheckListener = object : OnUpdateCheckListener {
        override fun onUpdateAvailable(update: Update) {
            LOGGER.info("New update is available")
            finishUpdatesCheck()
        }

        override fun onVersionUpToDate() {
            LOGGER.info("Current version is up to date")
            finishUpdatesCheck()
        }

        override fun onError(exception: Exception) {
            LOGGER.error("Got error while searching the newest version")
            finishUpdatesCheck()
        }
    }

    override fun onCreate() {
        IVPNApplication.getApplication().appComponent.inject(this)
        super.onCreate()
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        LOGGER.info("On start job...")
        this.jobParameters = jobParameters
        updateHelper?.subscribe(listener)
        updateHelper?.checkForUpdates()
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        LOGGER.error("On stop job...")
        return false
    }

    private fun finishUpdatesCheck() {
        updateHelper?.unsubscribe(listener)
        jobFinished(jobParameters, false)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UpdatesJobService::class.java)
    }
}