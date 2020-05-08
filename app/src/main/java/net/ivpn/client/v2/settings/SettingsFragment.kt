package net.ivpn.client.v2.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.common.nightmode.OnNightModeChangedListener
import net.ivpn.client.databinding.FragmentSettingsBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.settings.AdvancedKillSwitchActionListener
import net.ivpn.client.ui.settings.SettingsActivity
import net.ivpn.client.v2.dialog.DialogBuilderK
import net.ivpn.client.v2.viewmodel.*
import net.ivpn.client.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SettingsFragment : Fragment(), KillSwitchViewModel.KillSwitchNavigator,
        AdvancedKillSwitchActionListener, OnNightModeChangedListener, ColorThemeViewModel.ColorThemeNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SettingsActivity::class.java)
    }

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var startOnBoot: StartOnBootViewModel

    @Inject
    lateinit var alwaysOnVPN: AlwaysOnVPNViewModel

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    @Inject
    lateinit var killSwitch: KillSwitchViewModel

    @Inject
    lateinit var logging: LoggingViewModel

    @Inject
    lateinit var updates: UpdatesViewModel

    @Inject
    lateinit var colorTheme: ColorThemeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            LOGGER.debug("onActivityResult: RESULT_CANCELED")
            return
        }
        LOGGER.debug("onActivityResult: RESULT_OK")

        when (requestCode) {
            ServiceConstants.ENABLE_KILL_SWITCH -> {
                LOGGER.debug("onActivityResult: ENABLE_KILL_SWITCH")
                killSwitch.enable(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        servers.onResume()
        multihop.onResume()
        startOnBoot.onResume()
        alwaysOnVPN.onResume()
        antiTracker.onResume()
        killSwitch.onResume()
        updates.onResume()
        logging.onResume()
        colorTheme.onResume()
    }

    private fun initViews() {
        initToolbar()

        binding.contentLayout.multihop = multihop
        binding.contentLayout.servers = servers
        binding.contentLayout.startOnBoot = startOnBoot
        binding.contentLayout.alwaysOnVPN = alwaysOnVPN
        binding.contentLayout.antiTracker = antiTracker
        binding.contentLayout.killSwitch = killSwitch
        binding.contentLayout.updates = updates
        binding.contentLayout.logging = logging
        binding.contentLayout.colorTheme = colorTheme

        colorTheme.navigator = this

        initNavigation()
    }

    private fun initNavigation() {
        binding.contentLayout.sectionInterface.colorThemeLayout.setOnClickListener {
            openColorThemeDialogue()
        }
        binding.contentLayout.sectionOther.splitTunnelingLayout.setOnClickListener {
            openSplitTunnelingScreen()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openColorThemeDialogue() {
        DialogBuilderK.openDarkModeDialogue(context!!, this, colorTheme)
    }

    private fun openSplitTunnelingScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToSplitTunnelingFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun checkVPNPermission(requestCode: Int) {
        val intent: Intent?
        intent = try {
            VpnService.prepare(context)
        } catch (exception: Exception) {
            exception.printStackTrace()
            DialogBuilder.createNotificationDialog(context, Dialogs.FIRMWARE_ERROR)
            return
        }
        if (intent != null) {
            try {
                startActivityForResult(intent, requestCode)
            } catch (exception: ActivityNotFoundException) {
                LOGGER.info("startVpnFromIntent: intent != null, ActivityNotFoundException")
            }
        } else {
            onActivityResult(requestCode, Activity.RESULT_OK, null)
        }
    }

    override fun subscribe() {
    }

    override fun authenticate() {
    }

    override fun tryEnableKillSwitch(state: Boolean, advancedKillSwitchState: Boolean) {
        LOGGER.info("enableKillSwitch = $state isAdvancedKillSwitchDialogEnabled = $advancedKillSwitchState")
        if (state) {
            checkVPNPermission(ServiceConstants.ENABLE_KILL_SWITCH)
            if (killSwitch.isAdvancedModeSupported) {
                DialogBuilder.createAdvancedKillSwitchDialog(context, this)
            }
        } else {
            killSwitch.enable(false)
        }
    }

    override fun enableAdvancedKillSwitchDialog(enable: Boolean) {
        LOGGER.info("enableAdvancedKillSwitchDialog")
        killSwitch.enableAdvancedKillSwitchDialog(enable)
    }

    @SuppressLint("InlinedApi")
    override fun openDeviceSettings() {
        LOGGER.info("openDeviceSettings")
        if (killSwitch.isAdvancedModeSupported) {
            try {
                startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
            } catch (exception: ActivityNotFoundException) {
                DialogBuilder.createNotificationDialog(context, Dialogs.NO_VPN_SETTINGS)
            }
        }
    }

    override fun onNightModeChanged(mode: NightMode?) {
        if (mode == null) {
            return
        }
        AppCompatDelegate.setDefaultNightMode(mode.systemId)
        println("$mode was selected")
    }

    override fun onNightModeCancelClicked() {
    }
}