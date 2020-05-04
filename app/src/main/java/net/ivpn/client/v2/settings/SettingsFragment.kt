package net.ivpn.client.v2.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentSettingsBinding
import net.ivpn.client.v2.viewmodel.AlwaysOnVPNViewModel
import net.ivpn.client.v2.viewmodel.MultiHopViewModel
import net.ivpn.client.v2.viewmodel.ServersViewModel
import net.ivpn.client.v2.viewmodel.StartOnBootViewModel
import javax.inject.Inject

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var servers: ServersViewModel

    @Inject
    lateinit var multihop: MultiHopViewModel

    @Inject
    lateinit var startOnBoot: StartOnBootViewModel

    @Inject
    lateinit var alwaysOnVPNViewModel: AlwaysOnVPNViewModel

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

    override fun onResume() {
        super.onResume()
        servers.onResume()
    }

    private fun initViews() {
        initToolbar()

        binding.contentLayout.multihop = multihop
        binding.contentLayout.servers = servers
        binding.contentLayout.startOnBoot = startOnBoot
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}