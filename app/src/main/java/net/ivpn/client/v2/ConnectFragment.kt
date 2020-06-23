package net.ivpn.client.v2

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.databinding.FragmentConnectBinding
import net.ivpn.client.ui.connect.ConnectionNavigator
import net.ivpn.client.ui.connect.ConnectionState
import net.ivpn.client.ui.connect.CreateSessionFragment
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.protocol.ProtocolViewModel
import net.ivpn.client.v2.map.model.Location
import net.ivpn.client.v2.network.NetworkViewModel
import net.ivpn.client.v2.viewmodel.*
import net.ivpn.client.vpn.ServiceConstants
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ConnectFragment : Fragment(), MultiHopViewModel.MultiHopNavigator,
        ConnectionNavigator, LocationViewModel.LocationNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectFragment::class.java)
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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        println("Connect Fragment onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Connect Fragment onViewCreated")
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    private fun initViews() {
        println("Connect Fragment init views height = ${view?.height}")
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
                    checkLocation()
                }
            }
        })

        multihop.navigator = this
        connect.navigator = this
        location.navigator = this

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

        binding.accountButton.setOnClickListener {
            if (account.authenticated.get()) {
                openAccountScreen()
            } else {
                openLoginScreen()
            }
        }
        binding.slidingPanel.networkLayout.setOnClickListener() {
            openNetworkScreen()
        }
        binding.settingsButton.setOnClickListener {
            openSettingsScreen()
        }
        binding.slidingPanel.protocolLayout.setOnClickListener {
            openProtocolScreen()
        }
        binding.slidingPanel.enterServerLayout.setOnClickListener {
            openEnterServerSelectionScreen()
        }
        binding.slidingPanel.exitServerLayout.setOnClickListener {
            openExitServerSelectionScreen()
        }
        binding.slidingPanel.pauseButton.setOnClickListener {
            connect.onPauseRequest()
        }
        binding.slidingPanel.resumeButton.setOnClickListener {
            connect.onConnectRequest()
        }
    }

    override fun onResume() {
        println("Connect Fragment onResume")
        super.onResume()
        servers.onResume()
        account.onResume()
        applySlidingPanelSide()
        checkLocationPermission()
    }

    override fun onStart() {
        println("Connect Fragment onStart")
        super.onStart()
        network.onStart()
    }

    override fun onStop() {
        println("Connect Fragment onStop")
        super.onStop()
        network.onStop()
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
//                kill.startKillSwitch()
            }
        }
    }

    private fun applySlidingPanelSide() {
        if (multihop.isEnabled.get()) {
            enableMultiHop()
        } else {
            disableMultiHop()
        }
    }

    private fun checkLocation() {
        location.checkLocation(null)
    }

    private fun checkLocationPermission() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
//            return
//        }
//        val isPermissionGranted: Boolean = isPermissionGranted()
//        val isEnabled: Boolean = viewModel.areNetworkRulesEnabled.get()
//        if (!isEnabled) {
//            return
//        }
//        if (isPermissionGranted) {
//            viewModel.applyNetworkFeatureState(true)
//            return
//        }
//        askPermissionRationale()
    }

    private fun checkVPNPermission(requestCode: Int) {
        LOGGER.info("checkVPNPermission")
        val intent: Intent?
        intent = try {
            VpnService.prepare(context)
        } catch (exception: Exception) {
            exception.printStackTrace()
            openErrorDialog(Dialogs.FIRMWARE_ERROR)
            return
        }
        if (intent != null) {
            try {
                startActivityForResult(intent, requestCode)
            } catch (ane: ActivityNotFoundException) {
                LOGGER.error("Error while checking VPN permission", ane)
            }
        } else {
            onActivityResult(requestCode, Activity.RESULT_OK, null)
        }
    }

    override fun onMultiHopStateChanged(state: Boolean) {
        if (state) {
            enableMultiHop()
        } else {
            disableMultiHop()
        }
    }

    override fun subscribe() {

    }

    override fun authenticate() {
    }

    override fun notifyUser(msgId: Int, actionId: Int) {
    }

    private fun enableMultiHop() {
        bottomSheetBehavior.setPeekHeight((resources.getDimension(R.dimen.slider_layout_single_hop_height)
                + resources.getDimension(R.dimen.slider_layout_exit_layout_height)).toInt(), true)
        Handler().postDelayed({
            binding.slidingPanel.exitServerLayout.visibility = View.VISIBLE
        }, 50)
//        binding.slidingPanel.exitServerLayout.visibility = View.VISIBLE
        binding.slidingPanel.bottomSheet.requestLayout()
        binding.map.setPanelHeight((resources.getDimension(R.dimen.slider_layout_single_hop_height)
                + resources.getDimension(R.dimen.slider_layout_exit_layout_height)))
    }

    private fun disableMultiHop() {
        bottomSheetBehavior.setPeekHeight((resources.getDimension(R.dimen.slider_layout_single_hop_height)).toInt(), true)
        Handler().postDelayed({
            binding.slidingPanel.exitServerLayout.visibility = View.GONE
        }, 50)
//        binding.slidingPanel.exitServerLayout.visibility = View.GONE
        binding.slidingPanel.bottomSheet.requestLayout()
        binding.map.setPanelHeight(resources.getDimension(R.dimen.slider_layout_single_hop_height))
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
//        val entryServer = servers.entryServer.get()
//        entryServer?.let {server ->
//            if (state == ConnectionState.CONNECTED) {
//                binding.map.setConnectedLocation(Location(server.longitude.toFloat(), server.latitude.toFloat(), true))
//            } else if (state == ConnectionState.NOT_CONNECTED) {
//                binding.map.setConnectedLocation(null)
//            }
//        }
    }

    override fun askConnectionPermission() {
        checkVPNPermission(ServiceConstants.IVPN_REQUEST_CODE)
    }

    override fun notifyAnotherPortUsedToConnect() {
//        LOGGER.info("notifyAnotherPortUsedToConnect")
//        binding.contentLayout.connectionView.reset()
//        Handler().postDelayed({
//            SnackbarUtil.show(binding.coordinator, R.string.snackbar_new_try_with_different_port,
//                    R.string.snackbar_disconnect_first, null)
//        }, 500)
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
//        account.logout()
    }

    override fun initMapWith(location: Location) {
//        binding.map.setHomeLocation(location)
//        binding.map.visibility = View.VISIBLE
    }
}