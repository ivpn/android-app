package net.ivpn.client.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentConnectBinding
import net.ivpn.client.v2.viewmodel.MultiHopViewModel
import net.ivpn.client.v2.viewmodel.ServersViewModel
import javax.inject.Inject

class ConnectFragment : Fragment(), MultiHopViewModel.MultiHopNavigator {

    private lateinit var binding: FragmentConnectBinding

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
        view.post {
            binding.slidingPanel.scrollView.initWith(view.height, binding.slidingPanel.exitServerLayout)
        }
    }

    private fun initViews() {
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
        binding.slidingPanel.scrollView.showView()
    }

    private fun disableMultiHop() {
        binding.slidingPanel.scrollView.hideView()
    }
}