package net.ivpn.client.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
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

    var nightMode = AppCompatDelegate.MODE_NIGHT_NO
    private fun initViews() {
//        AppCompatDelegate.setDefaultNightMode(nightMode)
        binding.nightModeFab.setOnClickListener {
            nightMode = if (nightMode ==  AppCompatDelegate.MODE_NIGHT_NO) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        bottomSheetBehavior = from(binding.slidingPanel.sheetLayout)
        bottomSheetBehavior.state = STATE_COLLAPSED
        bottomSheetBehavior.halfExpandedRatio = 0.000000001f
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
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
}