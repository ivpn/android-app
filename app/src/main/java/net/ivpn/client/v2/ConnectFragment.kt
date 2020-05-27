package net.ivpn.client.v2

import android.os.Bundle
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
import net.ivpn.client.v2.viewmodel.MultiHopViewModel
import net.ivpn.client.v2.viewmodel.ServersViewModel
import javax.inject.Inject

class ConnectFragment : Fragment(), MultiHopViewModel.MultiHopNavigator {

    private lateinit var binding: FragmentConnectBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var servers: ServersViewModel

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

    private fun initViews() {
        bottomSheetBehavior = from(binding.slidingPanel.sheetLayout)
        bottomSheetBehavior.state = STATE_COLLAPSED
        bottomSheetBehavior.halfExpandedRatio = 0.000000001f
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN || newState == STATE_HALF_EXPANDED) {
                    bottomSheetBehavior.state = STATE_EXPANDED
                }
            }
        })

        multihop.navigator = this
        binding.slidingPanel.multihop = multihop

        binding.slidingPanel.servers = servers

        binding.accountButton.setOnClickListener {
            openAccountScreen()
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
    }

    override fun onResume() {
        super.onResume()
        servers.onResume()
        applySlidingPanelSide()
    }

    private fun applySlidingPanelSide() {
        if (multihop.isEnabled.get()) {
            enableMultiHop()
        } else {
            disableMultiHop()
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
        binding.slidingPanel.exitServerLayout.visibility = View.VISIBLE
        binding.slidingPanel.bottomSheet.requestLayout()
    }

    private fun disableMultiHop() {
        bottomSheetBehavior.setPeekHeight((resources.getDimension(R.dimen.slider_layout_single_hop_height)).toInt(), true)
        binding.slidingPanel.exitServerLayout.visibility = View.GONE
        binding.slidingPanel.bottomSheet.requestLayout()
    }

    private fun openSettingsScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToSettingsFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun openAccountScreen() {
        val action = ConnectFragmentDirections.actionConnectFragmentToLoginFragment()
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
}