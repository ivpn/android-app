package net.ivpn.client.v2.serverlist

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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
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
import net.ivpn.client.v2.MainActivity
import net.ivpn.client.v2.dialog.DialogBuilderK
import net.ivpn.client.v2.serverlist.dialog.Filters
import net.ivpn.client.v2.viewmodel.ServerListFilterViewModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ServerListTabFragment : Fragment(), ServerListFilterViewModel.OnFilterChangedListener {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ServerListTabFragment::class.java)
    }

    lateinit var binding: FragmentTabsServerListBinding

    @Inject
    lateinit var viewModel: ServersListCommonViewModel

    @Inject
    lateinit var filterViewModel: ServerListFilterViewModel

    lateinit var adapter: ServersListPagerAdapter
    val args: ServerListTabFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
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
        filterViewModel.onResume()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
                it.setFullScreen(false)
            }
        }
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

        binding.toolbar.inflateMenu(R.menu.menu_servers)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_filter) {
                openFilterDialogue()
            }
            return@setOnMenuItemClickListener true
        }
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openFilterDialogue() {
        DialogBuilderK.openSortServerDialogue(requireContext(), this, filterViewModel)
    }

    fun navigateBack() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    fun openFastestSetting() {
        val action = ServerListTabFragmentDirections.actionServerListFragmentToFastestSettingFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    fun getServerType(): ServerType {
        return args.serverType
    }

    override fun onFilterChanged(filter: Filters?) {
    }
}