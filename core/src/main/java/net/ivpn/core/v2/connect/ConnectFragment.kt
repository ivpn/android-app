package net.ivpn.core.v2.connect

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

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.FOCUS_UP
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.common.extension.checkVPNPermission
import net.ivpn.core.common.extension.findNavControllerSafely
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.utils.ToastUtil
import net.ivpn.core.databinding.FragmentConnectBinding
import net.ivpn.core.rest.data.model.ServerLocation
import net.ivpn.core.v2.connect.createSession.ConnectionNavigator
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.v2.connect.createSession.CreateSessionFragment
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.viewmodel.ProtocolViewModel
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.map.MapView
import net.ivpn.core.v2.map.model.Location
import net.ivpn.core.v2.network.NetworkViewModel
import net.ivpn.core.v2.signup.SignUpController
import net.ivpn.core.v2.viewmodel.*
import net.ivpn.core.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ConnectFragment : Fragment(), MultiHopViewModel.MultiHopNavigator,
        ConnectionNavigator, MapDialogs.GatewayListener, MapDialogs.LocationListener,
        LocationViewModel.LocationUpdatesUIListener {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectFragment::class.java)
        private const val LOCATION_PERMISSION_CODE = 133

        const val CONNECT_BY_MAP = 121
    }

    private lateinit var binding: FragmentConnectBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var createSessionFragment: CreateSessionFragment

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var location: LocationViewModel

    @Inject
    lateinit var protocol: ProtocolViewModel

    @Inject
    lateinit var network: NetworkViewModel

    @Inject
    lateinit var connect: ConnectionViewModel

    @Inject
    lateinit var killswitch: KillSwitchViewModel

    var signUp: SignUpController = IVPNApplication.signUpController

    var mapPopup: PopupWindow? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.info("On view created")
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    override fun onResume() {
        LOGGER.info("onResume: Connect fragment")
        super.onResume()
        servers.onResume()
        account.onResume()
        multihop.onResume()
        account.updateSessionStatus()
        checkLocationPermission()
        applySlidingPanelSide()

        LOGGER.info("translationY = ${binding.slidingPanel.sheetLayout.translationY}")
    }

    override fun onStart() {
        LOGGER.info("onStart: Connect fragment")
        super.onStart()
        location.addLocationListener(binding.map.locationListener)
        if (isPermissionGranted()) {
            network.updateNetworkSource(context)
        }
        if (killswitch.isEnabled.get()) {
            checkVPNPermission(ServiceConstants.KILL_SWITCH_REQUEST_CODE)
        }
        activity?.let {
            if (it is MainActivity) {
                it.setAdjustNothingMode()
                it.setContentSecure(false)
            }
        }
    }

    override fun onStop() {
        LOGGER.info("onStop: Connect fragment")
        super.onStop()
        location.removeLocationListener(binding.map.locationListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            LOGGER.info("onActivityResult: RESULT_CANCELED")
            return
        }

        LOGGER.info("onActivityResult: RESULT_OK")
        when (requestCode) {
            ServiceConstants.IVPN_REQUEST_CODE -> {
                connect.onConnectRequest()
            }
            ServiceConstants.KILL_SWITCH_REQUEST_CODE -> {
                LOGGER.debug("onActivityResult: ENABLE_KILL_SWITCH")
                killswitch.enable(true)
            }
            CONNECT_BY_MAP -> {
                connect.connectOrReconnect()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapPopup?.dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        LOGGER.info("onRequestPermissionsResult requestCode = $requestCode")
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isEmpty()) {
                    return
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    network.applyNetworkFeatureState(true)
                    network.updateNetworkSource(context)
                    return
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    network.applyNetworkFeatureState(false)
                    return
                }
            }
        }
    }

    private fun initViews() {
        initSlidingPanel()

        multihop.navigator = this
        connect.navigator = this
        location.uiListener = this

        binding.location = location
        binding.connection = connect
        binding.servers = servers
        binding.account = account
        binding.slidingPanel.antitracker = antiTracker
        binding.slidingPanel.multihop = multihop
        binding.slidingPanel.servers = servers
        binding.slidingPanel.protocol = protocol
        binding.slidingPanel.network = network
        binding.slidingPanel.connect = connect
        binding.slidingPanel.cards.location = location

        binding.lifecycleOwner = this

        servers.fastestServer.observe(viewLifecycleOwner) {
            binding.slidingPanel.fastestServer = it
        }

        initNavigation()
    }

    private fun initSlidingPanel() {
        LOGGER.info("Init sliding panel")
        bottomSheetBehavior = from(binding.slidingPanel.sheetLayout)
        bottomSheetBehavior.saveFlags = SAVE_NONE
        bottomSheetBehavior.state = STATE_COLLAPSED
        bottomSheetBehavior.halfExpandedRatio = 0.000000001f
        bottomSheetBehavior.expandedOffset = resources.getDimension(R.dimen.slider_panel_top_offset).toInt()
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_HIDDEN, STATE_HALF_EXPANDED -> {
                        bottomSheetBehavior.state = STATE_EXPANDED
                    }
                    STATE_EXPANDED -> {
                    }
                    STATE_COLLAPSED -> {
                        binding.slidingPanel.bottomSheet.fullScroll(FOCUS_UP)
                    }
                    else -> {
                    }
                }
            }
        })
        binding.slidingPanel.comparisonText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initNavigation() {
        binding.accountButton.setOnClickListener {
            if (account.authenticated.get()) {
                openAccountScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openLoginScreen()
            }
        }
        binding.slidingPanel.networkLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }

            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }

            openNetworkScreen()
        }
        binding.settingsButton.setOnClickListener {
            openSettingsScreen()
        }
        binding.slidingPanel.protocolLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
                return@setOnClickListener
            }

            if (!account.isActive.get()) {
                openAddFundsScreen()
                return@setOnClickListener
            }

            if (connect.isVpnActive()) {
                notifyUser(R.string.snackbar_to_change_protocol_disconnect,
                        R.string.snackbar_disconnect_first)
                return@setOnClickListener
            }

            openProtocolScreen()
        }
        binding.slidingPanel.enterServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEnterServerSelectionScreen()
            }
        }
        binding.slidingPanel.exitServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openExitServerSelectionScreen()
            }
        }
        binding.slidingPanel.fastestServerLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEnterServerSelectionScreen()
            }
        }
        binding.slidingPanel.entryRandomLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openEnterServerSelectionScreen()
            }
        }
        binding.slidingPanel.exitRandomLayout.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                openExitServerSelectionScreen()
            }
        }
        binding.slidingPanel.pauseButton.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                connect.onPauseRequest()
            }
        }
        binding.slidingPanel.resumeButton.setOnClickListener {
            if (!account.authenticated.get()) {
                openLoginScreen()
            } else if (!account.isActive.get()) {
                openAddFundsScreen()
            } else {
                connect.onConnectRequest()
            }
        }
        binding.centerLocation.setOnClickListener {
            binding.map.centerMap()
        }

        binding.renew1.setOnClickListener {
            openAddFundsScreen()
        }

        binding.renew2.setOnClickListener {
            openAddFundsScreen()
        }

        binding.slidingPanel.antitrackerSwitch.setOnTouchListener { _, event ->
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
                    ToastUtil.toast(context, R.string.snackbar_to_use_antitracker_disconnect)
                }
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }

        binding.map.mapListener = object : MapView.MapListener {
            override fun openLocationDialogue(location: Location?) {
                view?.let {
                    val topMargin = (it.height - peekHeight) / 2f + resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)
                    if (connect.connectionState.get() != null && connect.connectionState.get() == ConnectionState.PAUSED) {
                        mapPopup = MapDialogs.openPauseDialogue(it, connect, topMargin, this@ConnectFragment)
                    } else {
                        mapPopup = MapDialogs.openLocationDialogue(it, location, topMargin, this@ConnectFragment)
                    }
                }
            }

            override fun openGatewayDialogue(list: ArrayList<ServerLocation>) {
                if (list.isEmpty()) {
                    return
                }

                val filteredList = filterLocation(list)

                view?.let {
                    val topMargin = (it.height - peekHeight) / 2f + resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)

                    val location: ServerLocation
                    when (filteredList.size) {
                        0 -> {
                            location = list[0]
                            mapPopup = MapDialogs.openForbiddenGatewayDialog(it, location, topMargin)
                        }
                        1 -> {
                            location = filteredList[0]
                            mapPopup = MapDialogs.openGatewayDialog(it, filteredList[0], topMargin, this@ConnectFragment)
                        }
                        else -> {
                            location = filteredList[0]
                            mapPopup = MapDialogs.openGatewayListDialog(it, list, topMargin, this@ConnectFragment)
                        }
                    }

                    if (!connect.isVpnActive()) {
                        servers.setServerLocation(location)
                    }
                }
            }
        }
    }

    private fun filterLocation(list: ArrayList<ServerLocation>): ArrayList<ServerLocation> {
        val filteredList = ArrayList<ServerLocation>()
        for (location in list) {
            if (servers.isLocationSuitable(location)) {
                filteredList.add(location)
            }
        }

        return filteredList
    }

    private fun applySlidingPanelSide() {
        recalculatePeekHeight()
    }

    override fun checkLocation() {
        this@ConnectFragment.location.checkLocation()
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    override fun resumeConnection() {
        connect.resume()
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return
        }
        val isEnabled: Boolean = network.isNetworkFeatureEnabled.get()
        if (!isEnabled) {
            return
        }
        val isPermissionGranted: Boolean = isPermissionGranted()
        LOGGER.info("isPermissionGranted = $isPermissionGranted")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val isBackgroundPermissionGranted: Boolean = isBackgroundLocationPermissionGranted()
            if (isBackgroundPermissionGranted) {
                network.applyNetworkFeatureState(true)
                return
            }
            askBackgroundPermissionRationale()
        } else {
            if (isForegroundLocationPermissionGranted()) {
                network.applyNetworkFeatureState(true)
                return
            }
            askPermissionRationale()
        }
    }

    private fun isForegroundLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun askPermissionRationale() {
        LOGGER.info("askPermissionRationale")
        DialogBuilder.createNonCancelableDialog(requireActivity(), Dialogs.ASK_LOCATION_PERMISSION,
                { _: DialogInterface?, _: Int -> goToAndroidAppSettings() },
                { network.applyNetworkFeatureState(false) })
    }

    private fun askBackgroundPermissionRationale() {
        LOGGER.info("askPermissionRationale")
        DialogBuilder.createNonCancelableDialog(requireActivity(), Dialogs.ASK_BACKGROUND_LOCATION_PERMISSION,
                { _: DialogInterface?, _: Int -> goToAndroidAppSettings() },
                { network.applyNetworkFeatureState(false) })
    }

    private fun showInformationDialog() {
        LOGGER.info("showInformationDialog")
        DialogBuilder.createNonCancelableDialog(requireActivity(), Dialogs.LOCATION_PERMISSION_INFO,
                null, { askPermission() })
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun shouldRequestRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun askPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
    }

    private fun goToAndroidAppSettings() {
        LOGGER.info("goToAndroidAppSettings")
        val action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.parse(getString(R.string.settings_package) + IVPNApplication.config.applicationId)
        val intent = Intent(action, uri)
        startActivity(intent)
    }

    override fun onMultiHopStateChanged(state: Boolean) {
        recalculatePeekHeight()
    }

    override fun subscribe() {
    }

    override fun authenticate() {
    }

    override fun notifyUser(msgId: Int, actionId: Int) {
        ToastUtil.toast(context, msgId)
    }

    var peekHeight: Float = 0f
    private fun recalculatePeekHeight() {
        if (context == null) {
            return
        }
        var alertHeight = 0f
        if (account.isExpiredIn.get() || account.isExpired.get() || location.isLocationAPIError.get()) {
            alertHeight = resources.getDimension(R.dimen.map_alert_height) + resources.getDimension(R.dimen.map_alert_vertical_margin)
        }
        peekHeight = resources.getDimension(R.dimen.slider_layout_basic_height)
        if (multihop.isEnabled.get()) {
            peekHeight += resources.getDimension(R.dimen.slider_layout_server_layout_height)
//            binding.slidingPanel.exitServerLayout.visibility = View.VISIBLE
        } else {
//            Handler().postDelayed({
//                binding.slidingPanel.exitServerLayout.visibility = View.GONE
//            }, 50)
        }
        if (multihop.isSupported.get()) {
            peekHeight += resources.getDimension(R.dimen.slider_layout_multihop_switch_height)
        }
        LOGGER.info("peekHeight = $peekHeight")
        bottomSheetBehavior.setPeekHeight(peekHeight.toInt(), true)
        binding.map.setPanelHeight(peekHeight - resources.getDimension(R.dimen.map_margin_bottom))
        binding.centerLocation.animate().translationY(-peekHeight - alertHeight)
        binding.alertsLayout.animate().translationY(-peekHeight)
    }

    private fun openSettingsScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToSettingsFragment()
        navigate(action)
    }

    private fun openNetworkScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToNetworkProtectionFragment()
        navigate(action)
    }

    private fun openLoginScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToLoginFragment()
        navigate(action)
    }

    private fun openAccountScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToAccountFragment()
        navigate(action)
    }

    private fun openProtocolScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToProtocolFragment()
        navigate(action)
    }

    private fun openEnterServerSelectionScreen() {
        println("backstack openEnterServerSelectionScreen")
        val action = ConnectFragmentDirections.actionConnectFragmentToServerListFragment(ServerType.ENTRY)
        navigate(action)
    }

    private fun openExitServerSelectionScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToServerListFragment(ServerType.EXIT)
        navigate(action)
    }

    private fun disconnectVpnService(needToReset: Boolean, dialog: Dialogs?,
                                     listener: DialogInterface.OnClickListener) {
        if (dialog != null) {
            DialogBuilder.createOptionDialog(context, dialog, listener)
        }
    }

    override fun openNoNetworkDialog() {
        openErrorDialog(Dialogs.CONNECTION_ERROR)
    }

    override fun openErrorDialog(dialogs: Dialogs) {
        DialogBuilder.createNotificationDialog(context, dialogs)
    }

    override fun onAuthFailed() {
        LOGGER.info("onAuthFailed")
        disconnectVpnService(true, Dialogs.ON_CONNECTION_AUTHENTICATION_ERROR
        ) { _: DialogInterface?, _: Int ->
            LOGGER.info("onClick: ")
            logout()
        }
    }

    override fun onChangeConnectionStatus(state: ConnectionState) {
    }

    override fun askConnectionPermission() {
        if (!account.authenticated.get()) {
            openLoginScreen()
        } else if (!account.isActive.get()) {
            openAddFundsScreen()
        } else {
            checkVPNPermission(ServiceConstants.IVPN_REQUEST_CODE)
        }
    }

    override fun notifyAnotherPortUsedToConnect() {
        Handler().postDelayed({
            if (context != null) {
                ToastUtil.toast(context, R.string.snackbar_new_try_with_different_port)
            }
        }, 500)
    }

    override fun accountVerificationFailed() {
        LOGGER.info("accountVerificationFailed")
        DialogBuilder.createNonCancelableDialog(context, Dialogs.SESSION_HAS_EXPIRED,
                { _: DialogInterface?, _: Int ->
                    LOGGER.info("onClick: ")
                    logout()
                },
                {
                    LOGGER.info("onCancel: ")
                    logout()
                })
    }

    override fun openSessionLimitReachedDialogue() {
        createSessionFragment =
                CreateSessionFragment()
        createSessionFragment.show(childFragmentManager, createSessionFragment.tag)
    }

    override fun onTimeOut() {
        LOGGER.info("onTimeOut")
        disconnectVpnService(true, Dialogs.TRY_RECONNECT,
                DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> connect.onConnectRequest() })
    }

    override fun logout() {
        recalculatePeekHeight()
    }

    override fun connectTo(location: ServerLocation) {
        if (!account.authenticated.get()) {
            openLoginScreen()
        } else if (!account.isActive.get()) {
            openAddFundsScreen()
        } else {
            servers.setServerLocation(location)
            checkVPNPermission(CONNECT_BY_MAP)
        }
    }

    override fun updateSelectionTo(location: ServerLocation) {
        if (!connect.isVpnActive()) {
            servers.setServerLocation(location)
        }
    }

    private fun openAddFundsScreen() {
        signUp.signUpWithInactiveAccount(findNavControllerSafely(),
                Plan.getPlanByProductName(account.accountType.get()), account.isAccountNewStyle())
    }

    override fun onLocationUpdated() {
        recalculatePeekHeight()
    }
}