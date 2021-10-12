package net.ivpn.core.v2.settings

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

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.Constant
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.common.extension.findNavControllerSafely
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.common.nightmode.NightMode
import net.ivpn.core.common.nightmode.OnNightModeChangedListener
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.utils.ToastUtil
import net.ivpn.core.databinding.FragmentSettingsBinding
import net.ivpn.core.v2.mocklocation.MockLocationNavigator
import net.ivpn.core.v2.mocklocation.MockLocationViewModel
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.dialog.DialogBuilderK
import net.ivpn.core.v2.signup.SignUpController
import net.ivpn.core.v2.updates.UpdatesController
import net.ivpn.core.v2.viewmodel.*
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class SettingsFragment : Fragment(), OnNightModeChangedListener, ColorThemeViewModel.ColorThemeNavigator, MockLocationNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SettingsFragment::class.java)
    }

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var startOnBoot: StartOnBootViewModel

    @Inject
    lateinit var alwaysOnVPN: AlwaysOnVPNViewModel

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    @Inject
    lateinit var logging: LoggingViewModel

    @Inject
    lateinit var colorTheme: ColorThemeViewModel

    @Inject
    lateinit var mockLocation: MockLocationViewModel

    @Inject
    lateinit var localBypass: BypassVpnViewModel

    @Inject
    lateinit var killSwitch: KillSwitchViewModel

    @Inject
    lateinit var ipv6: IPv6ViewModel

    var signUp: SignUpController = IVPNApplication.signUpController
    var updates =  IVPNApplication.updatesController

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    override fun onResume() {
        super.onResume()

        servers.onResume()
        multihop.onResume()
        startOnBoot.onResume()
        alwaysOnVPN.onResume()
        logging.onResume()
        colorTheme.onResume()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
            }
        }
    }

    private fun initViews() {
        initToolbar()

        binding.contentLayout.multihop = multihop
        binding.contentLayout.servers = servers
        binding.contentLayout.startOnBoot = startOnBoot
        binding.contentLayout.alwaysOnVPN = alwaysOnVPN
        binding.contentLayout.antiTracker = antiTracker
        binding.contentLayout.updates = updates
        binding.contentLayout.logging = logging
        binding.contentLayout.colorTheme = colorTheme
        binding.contentLayout.mocklocation = mockLocation
        binding.contentLayout.localbypass = localBypass
        binding.contentLayout.ipv6 = ipv6
        binding.contentLayout.killSwitch = killSwitch

        colorTheme.navigator = this
        mockLocation.navigator = this

        initNavigation()
    }

    private fun initNavigation() {
        binding.contentLayout.sectionOther.antiTrackerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }
            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }
            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_use_antitracker_disconnect)
                return@setOnClickListener
            }

            openAntiTrackerScreen()
        }
        binding.contentLayout.sectionInterface.colorThemeLayout.setOnClickListener {
            openColorThemeDialogue()
        }
        binding.contentLayout.sectionConnectivity.splitTunnelingLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }
            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }
            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_use_split_tunneling_disconnect)
                return@setOnClickListener
            }

            openSplitTunnelingScreen()
        }
        binding.contentLayout.sectionOther.alwaysOnVpn.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openAlwaysOnVPNScreen()
            }
        }
        binding.contentLayout.sectionOther.networkProtectionLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openNetworkProtectionScreen()
            }
        }
        binding.contentLayout.sectionServer.protocolLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }
            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }
            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_change_protocol_disconnect)
                return@setOnClickListener
            }

            openProtocolScreen()
        }
        binding.contentLayout.sectionConnectivity.customDns.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }
            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }
            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_use_custom_dns_disconnect)
                return@setOnClickListener
            }

            openCustomDNSScreen()
        }
        binding.contentLayout.sectionOther.killswitchLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }
            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }
            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_use_kill_switch_disconnect)
                return@setOnClickListener
            }

            openKillSwitchScreen()
        }
        binding.contentLayout.sectionAbout.termsOfServiceLayout.setOnClickListener {
            openTermsOfServiceScreen()
        }
        binding.contentLayout.sectionAbout.privacyPolicyLayout.setOnClickListener {
            openPrivacyPolicyScreen()
        }
        binding.contentLayout.sectionAbout.checkUpdatesLayout.setOnClickListener {
            openUpdatesScreen()
        }
        binding.contentLayout.sectionLogging.sendLogsLayout.setOnClickListener {
            sendLogs()
        }
        binding.contentLayout.sectionServer.entryServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEntryServerScreen()
            }
        }
        binding.contentLayout.sectionServer.entryRandomLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEntryServerScreen()
            }
        }
        binding.contentLayout.sectionServer.fastestServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEntryServerScreen()
            }
        }
        binding.contentLayout.sectionServer.exitServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openExitServerScreen()
            }
        }
        binding.contentLayout.sectionServer.exitRandomLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openExitServerScreen()
            }
        }
        binding.contentLayout.sectionConnectivity.localBypassSwitcher.setOnTouchListener { _, event ->
            if (!account.authenticated.get()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    openLoginScreen()
                }
                return@setOnTouchListener true
            } else if (!account.isActive.get()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    openAddFundsScreen()
                }
                return@setOnTouchListener true
            } else if (connect.isVpnActive()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    ToastUtil.toast(context, R.string.snackbar_to_use_vpn_bypass_disconnect)
                }
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }
        binding.contentLayout.sectionOther.mockLocationSwitcher.setOnTouchListener { _, event ->
            if (!account.authenticated.get()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    openLoginScreen()
                }
                return@setOnTouchListener true
            } else if (!account.isActive.get()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    openAddFundsScreen()
                }
                return@setOnTouchListener true
            } else if (connect.isVpnActive()) {
                if (event.action == MotionEvent.ACTION_UP) {
                    ToastUtil.toast(context, R.string.snackbar_to_use_mock_location_disconnect)
                }
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openColorThemeDialogue() {
        DialogBuilderK.openDarkModeDialogue(requireContext(), this, colorTheme)
    }

    private fun openSplitTunnelingScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToSplitTunnelingFragment()
        navigate(action)
    }

    private fun openAlwaysOnVPNScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToAlwaysOnVPNFragment()
        navigate(action)
    }

    private fun openNetworkProtectionScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToNetworkProtectionFragment()
        navigate(action)
    }

    private fun openAntiTrackerScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToAntiTrackerFragment()
        navigate(action)
    }

    private fun openProtocolScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToProtocolFragment()
        navigate(action)
    }

    private fun openCustomDNSScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToCustomDNSFragment()
        navigate(action)
    }

    private fun openKillSwitchScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToKillSwitchFragment()
        navigate(action)
    }

    private fun openTermsOfServiceScreen() {
        openWebPage("https://www.ivpn.net/tos-mobile-app/")
    }

    private fun openPrivacyPolicyScreen() {
        openWebPage("https://www.ivpn.net/privacy-mobile-app/")
    }

    private fun openUpdatesScreen() {
        updates.openUpdatesScreen(findNavControllerSafely())
//        val action = SettingsFragmentDirections.actionSettingsFragmentToUpdatesFragment()
//        navigate(action)
    }

    private fun openWebPage(urlString: String) {
        val openURL = Intent(android.content.Intent.ACTION_VIEW)
        openURL.data = Uri.parse(urlString)
        startActivity(openURL)
    }

    private fun sendLogs() {
        val uris = ArrayList<Uri>()
        val uri: Uri = logging.getLogFileUri(context)
        uris.add(uri)

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Constant.SUPPORT_EMAIL))
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        activity?.let {
            val possibleActivitiesList: List<ResolveInfo> =
                    it.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            if (possibleActivitiesList.size > 1) {
                val chooser = resources.getString(R.string.send_logs).let { title ->
                    Intent.createChooser(intent, title)
                }
                startActivity(chooser)
            } else if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent)
            }
        } ?: kotlin.run {
            startActivity(intent)
        }
    }

    private fun openEntryServerScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToServerListFragment(ServerType.ENTRY)
        navigate(action)
    }

    private fun openSetupMockLocation() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToMockLocationFragment()
        navigate(action)
    }

    private fun openExitServerScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToServerListFragment(ServerType.EXIT)
        navigate(action)
    }

    private fun openLoginScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        navigate(action)
    }

    private fun notifyUser(msgId: Int) {
        ToastUtil.toast(context, msgId)
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

    private fun openAddFundsScreen() {
            signUp.signUpWithInactiveAccount(findNavControllerSafely(),
                    Plan.getPlanByProductName(account.accountType.get()), account.isAccountNewStyle())
    }

    override fun setupMockLocation() {
        openSetupMockLocation()
    }
}