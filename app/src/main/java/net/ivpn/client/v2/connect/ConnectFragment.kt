package net.ivpn.client.v2.connect

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import net.ivpn.client.BuildConfig
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.SnackbarUtil
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.common.extension.checkVPNPermission
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.utils.ToastUtil
import net.ivpn.client.databinding.FragmentConnectBinding
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.ui.connect.ConnectionNavigator
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.ui.connect.CreateSessionFragment
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.protocol.ProtocolViewModel
import net.ivpn.client.v2.login.LoginFragmentDirections
import net.ivpn.client.v2.map.MapView
import net.ivpn.client.v2.map.model.Location
import net.ivpn.client.v2.network.NetworkViewModel
import net.ivpn.client.v2.viewmodel.*
import net.ivpn.client.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ConnectFragment : Fragment(), MultiHopViewModel.MultiHopNavigator,
        ConnectionNavigator, MapDialogs.GatewayListener, MapDialogs.LocationListener {

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
    lateinit var signUp: SignUpViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
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

        binding.location = location
        binding.connection = connect
        binding.servers = servers
        binding.slidingPanel.antitracker = antiTracker
        binding.slidingPanel.multihop = multihop
        binding.slidingPanel.servers = servers
        binding.slidingPanel.protocol = protocol
        binding.slidingPanel.network = network
        binding.slidingPanel.connect = connect
        binding.slidingPanel.cards.location = location

//        binding.slidingPanel.cards.internetProviderValue.isSelected = true

        initNavigation()
    }

    private fun initSlidingPanel() {
        bottomSheetBehavior = from(binding.slidingPanel.sheetLayout)
        bottomSheetBehavior.state = STATE_COLLAPSED
        bottomSheetBehavior.halfExpandedRatio = 0.000000001f
        bottomSheetBehavior.setExpandedOffset(100)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN || newState == STATE_HALF_EXPANDED) {
                    bottomSheetBehavior.state = STATE_EXPANDED
                } else if (newState == STATE_EXPANDED) {
//                    checkLocation()
                }
            }
        })
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

        binding.map.mapListener = object : MapView.MapListener {
            override fun openLocationDialogue(location: Location?) {
                view?.let {
                    val topMargin = (it.height - peekHeight) / 2f + resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)
                    if (connect.connectionState.get() != null && connect.connectionState.get() == ConnectionState.PAUSED) {
                        MapDialogs.openPauseDialogue(it, connect, topMargin, this@ConnectFragment)
                    } else {
                        MapDialogs.openLocationDialogue(it, location, topMargin, this@ConnectFragment)
                    }
                }
            }

            override fun openGatewayDialogue(list: ArrayList<ServerLocation>) {
                if (list.size == 1) {
                    handlePossibleLocation(list[0])
                } else {
                    handlePossibleLocationList(list)
                }
            }
        }
    }

    private fun handlePossibleLocation(location: ServerLocation) {
        view?.let {
            val topMargin = (it.height - peekHeight) / 2f + resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)
            if (servers.isLocationSuitable(location)) {
                MapDialogs.openGatewayDialog(it, location, topMargin, this@ConnectFragment)
            } else {
                MapDialogs.openForbiddenGatewayDialog(it, location, topMargin)
            }
            if (!connect.isVpnActive()) {
                servers.setServerLocation(location)
            }
        }
    }

    private fun handlePossibleLocationList(list: ArrayList<ServerLocation>) {
        view?.let {
            val topMargin = (it.height - peekHeight) / 2f + resources.getDimension(R.dimen.map_dialog_inner_vertical_margin)

            var forbiddenLocation: ServerLocation? = null
            for (location in list) {
                if (!servers.isLocationSuitable(location)) {
                    forbiddenLocation = location
                    break
                }
            }

            forbiddenLocation?.also { forbiddenLocationObj ->
                list.remove(forbiddenLocationObj)
                if (list.size == 1) {
                    MapDialogs.openGatewayDialog(it, list[0], topMargin, this@ConnectFragment)
                } else {
                    MapDialogs.openGatewayListDialog(it, list, topMargin, this@ConnectFragment)
                }
            } ?: run {
                MapDialogs.openGatewayListDialog(it, list, topMargin, this@ConnectFragment)
            }

            if (!connect.isVpnActive()) {
                servers.setServerLocation(list[0])
            }
        }
    }

    override fun onResume() {
        LOGGER.info("onResume: Connect fragment")
        super.onResume()
        servers.onResume()
        account.onResume()
        multihop.onResume()
        applySlidingPanelSide()
        checkLocationPermission()
        account.updateSessionStatus()
        checkLocationPermission()
    }

    override fun onStart() {
        LOGGER.info("onStart: Connect fragment")
        super.onStart()
        location.addLocationListener(binding.map.locationListener)
        if (isPermissionGranted()) {
            network.updateNetworkSource(context)
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
            }
            CONNECT_BY_MAP -> {
                connect.connectOrReconnect()
            }
        }
    }

    private fun applySlidingPanelSide() {
        recalculatePeekHeight()
    }

    override fun checkLocation() {
        this@ConnectFragment.location.checkLocation()
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    override fun resumeConnection() {
        connect.onConnectRequest()
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
        if (isPermissionGranted) {
            network.applyNetworkFeatureState(true)
            return
        }
        askPermissionRationale()
    }

    fun shouldAskForLocationPermission(): Boolean {
        LOGGER.info("shouldAskForLocationPermission")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return false
        }
        if (isPermissionGranted()) {
            return false
        }
        if (shouldRequestRationale()) {
            askPermissionRationale()
        } else {
            showInformationDialog()
        }
        return true
    }

    private fun askPermissionRationale() {
        LOGGER.info("askPermissionRationale")
        DialogBuilder.createNonCancelableDialog(activity!!, Dialogs.ASK_LOCATION_PERMISSION,
                { _: DialogInterface?, _: Int -> goToAndroidAppSettings() },
                { network.applyNetworkFeatureState(false) })
    }

    private fun showInformationDialog() {
        LOGGER.info("showInformationDialog")
        DialogBuilder.createNonCancelableDialog(activity!!, Dialogs.LOCATION_PERMISSION_INFO,
                null, { askPermission() })
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!,
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
        val uri = Uri.parse(getString(R.string.settings_package) + BuildConfig.APPLICATION_ID)
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
        peekHeight = resources.getDimension(R.dimen.slider_layout_basic_height)
        if (multihop.isEnabled.get()) {
            peekHeight += resources.getDimension(R.dimen.slider_layout_exit_layout_height)
            binding.slidingPanel.exitServerLayout.visibility = View.VISIBLE
        } else {
            Handler().postDelayed({
                binding.slidingPanel.exitServerLayout.visibility = View.GONE
            }, 50)
        }
        if (multihop.isSupported.get()) {
            peekHeight += resources.getDimension(R.dimen.slider_layout_multihop_switch_height)
        }
        bottomSheetBehavior.setPeekHeight(peekHeight.toInt(), true)
        binding.map.setPanelHeight(peekHeight)
        binding.centerLocation.animate().translationY(-peekHeight)
    }

    private fun openSettingsScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToSettingsFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openNetworkScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToNetworkProtectionFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openLoginScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToLoginFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openAccountScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToAccountFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openProtocolScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToProtocolFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openEnterServerSelectionScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToServerListFragment(ServerType.ENTRY)
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openExitServerSelectionScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToServerListFragment(ServerType.EXIT)
        NavHostFragment.findNavController(this).navigate(action)
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
        disconnectVpnService(true, Dialogs.ON_CONNECTION_AUTHENTICATION_ERROR,
                DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                    LOGGER.info("onClick: ")
                    logout()
                })
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
            SnackbarUtil.show(binding.mainContent, R.string.snackbar_new_try_with_different_port,
                    R.string.snackbar_disconnect_first, null)
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
        createSessionFragment = CreateSessionFragment()
        createSessionFragment.show(childFragmentManager, createSessionFragment.tag)
    }

    override fun onTimeOut() {
        LOGGER.info("onTimeOut")
        disconnectVpnService(true, Dialogs.TRY_RECONNECT,
                DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> connect.onConnectRequest() })
    }

    override fun logout() {
        recalculatePeekHeight()
//        account.logOut()
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
        if (account.isAccountNewStyle()) {
            signUp.selectedPlan.set(Plan.getPlanByProductName(account.accountType.get()))

            val action = ConnectFragmentDirections.actionConnectFragmentToSignUpPeriodFragment()
            NavHostFragment.findNavController(this).navigate(action)
        } else {
            openAddFundsSite()
        }
    }

    private fun openAddFundsSite() {
        val url = "https://www.ivpn.net/account/login"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}