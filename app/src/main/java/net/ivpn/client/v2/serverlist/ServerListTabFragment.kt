package net.ivpn.client.v2.serverlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.databinding.FragmentTabsServerListBinding
import net.ivpn.client.ui.serverlist.ServersListCommonViewModel
import net.ivpn.client.ui.serverlist.ServersListPagerAdapter
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ServerListTabFragment : Fragment() {

    companion object {
        val LOGGER = LoggerFactory.getLogger(ServerListTabFragment::class.java)
    }

    lateinit var binding: FragmentTabsServerListBinding

    @Inject
    lateinit var viewModel: ServersListCommonViewModel
    lateinit var adapter: ServersListPagerAdapter
    val args: ServerListTabFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tabs_server_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        LOGGER.info("onResume")
        viewModel.onResume()
    }

    private fun initViews() {
        binding.viewmodel = viewModel
        adapter = ServersListPagerAdapter(context, childFragmentManager)
        binding.pager.adapter = adapter
        binding.slidingTabs.setupWithViewPager(binding.pager)

        viewModel.start(context, args.serverType)
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    fun navigateBack() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    fun onFastestServerSelected() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    fun openFastestSetting() {
        val action = ServerListTabFragmentDirections.actionServerListFragmentToFastestSettingFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    fun getServerType(): ServerType {
        return args.serverType
    }
}