package net.ivpn.client.common.updater

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.ivpn.client.BuildConfig
import net.ivpn.client.IVPNApplication
import net.ivpn.client.common.Constant
import net.ivpn.client.common.Mapper
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.ui.updates.UpdatesActivity
import net.ivpn.client.ui.updates.UpdatesService
import net.ivpn.client.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import java.net.URL
import javax.inject.Inject
import java.util.*

@ApplicationScope
class UpdateHelper @Inject constructor(
        private val settings: Settings) {

    private val LOGGER = LoggerFactory.getLogger(UpdateHelper::class.java)
    private var notificationActionReceiver: BroadcastReceiver? = null

    private val currentVersion: String = BuildConfig.VERSION_NAME
    private val listeners: MutableList<OnUpdateCheckListener> = Collections.synchronizedList(mutableListOf())
    private var updateURL:Uri? = null;

    fun checkForUpdates() {
        LOGGER.info("Checking for updates")
        GlobalScope.async {
            try {
                val inputAsString = URL(Constant.UPDATE_URL).readText()
                process(inputAsString)
            } catch (exception: Exception) {
                notifyError(exception)
            }
        }
    }

    fun getCurrentVersion() : String {
        return currentVersion
    }

    fun notifyAboutAvailableVersion() {
        val newestVersionJson = settings.nextVersion
        val update: Update? = Mapper.updateFrom(newestVersionJson)

        update?.latestVersion?.let {
            if (currentVersion != it) {
                updateURL = Uri.parse(update.url)
                notifyAboutNewVersion(update)
            }
        }
    }

    fun subscribe(listener: OnUpdateCheckListener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: OnUpdateCheckListener) {
        listeners.remove(listener)
    }

    private fun startService() {
        LOGGER.info("Starting service...")
        val context = IVPNApplication.getApplication()
        val intent = Intent(context, UpdatesService::class.java)
        intent.action = ServiceConstants.SHOW_UPDATE_NOTIFICATION
        startService(context, intent)
    }

    private fun startService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopService() {
        if (!UpdatesService.isRunning.get()) {
            return
        }
        LOGGER.info("Stopping service...")
        val context = IVPNApplication.getApplication()
        val intent = Intent(context, UpdatesService::class.java)
        intent.action = ServiceConstants.CANCEL_UPDATE_NOTIFICATION
        startService(context, intent)
    }

    private fun onNotificationAction(intent: Intent) {
        val actionExtra = intent.getStringExtra(ServiceConstants.UPDATE_NOTIFICATION_ACTION_EXTRA)
                ?: return
        LOGGER.info("onNotificationAction, actionExtra = $actionExtra")
        when (actionExtra) {
            ServiceConstants.UPDATE_PROCEED -> {
                proceedUpdate()
            }
            ServiceConstants.UPDATE_SKIP -> {
                skipUpdate()
            }
            ServiceConstants.UPDATE_SETTINGS -> {
                openSettings()
            }
        }
    }

    fun proceedUpdate() {
        LOGGER.info("Proceed")

        val context = IVPNApplication.getApplication()

        LOGGER.info("Open url = ${updateURL.toString()}")
        val intent = Intent(Intent.ACTION_VIEW, updateURL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
        skipUpdate()
    }

    fun skipUpdate() {
        stopService()
        unregisterReceivers()
    }

    private fun openSettings() {
        LOGGER.info("Open settings")

        val context = IVPNApplication.getApplication()

        val intent = Intent(context, UpdatesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        context.startActivity(intent)
        skipUpdate()
    }

    private fun registerReceiver() {
        LOGGER.info("Register receiver")
        if (notificationActionReceiver != null) return

        notificationActionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action ?: return
                if (action == ServiceConstants.UPDATE_NOTIFICATION_ACTION) {
                    onNotificationAction(intent)
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ServiceConstants.UPDATE_NOTIFICATION_ACTION)

        IVPNApplication.getApplication().registerReceiver(notificationActionReceiver, intentFilter)
    }

    private fun unregisterReceivers() {
        LOGGER.info("Unregister receiver")

        if (notificationActionReceiver == null) return
        IVPNApplication.getApplication().unregisterReceiver(notificationActionReceiver)
        notificationActionReceiver = null
    }

    private fun process(updateJson: String) {
        LOGGER.info("Processing json with updates... $updateJson")

        val update: Update? = Mapper.updateFrom(updateJson)

        update?.latestVersion?.let {
            if (it > currentVersion) {
                registerReceiver()
                startService()
                settings.nextVersion = updateJson
                updateURL = Uri.parse(update.url)
                notifyAboutNewVersion(update)
            } else {
                notifyUpToDate()
            }
        } ?: run {
            notifyUpToDate()
        }
    }

    private fun notifyUpToDate() {
        listeners.forEach { it.onVersionUpToDate() }
    }

    private fun notifyAboutNewVersion(update: Update) {
        listeners.forEach { it.onUpdateAvailable(update) }
    }

    private fun notifyError(exception: Exception) {
        listeners.forEach { it.onError(exception) }
    }
}