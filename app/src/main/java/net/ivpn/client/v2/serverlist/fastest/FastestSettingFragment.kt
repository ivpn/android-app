package net.ivpn.client.v2.serverlist.fastest

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.utils.ToastUtil
import net.ivpn.client.databinding.FragmentFastestSettingsBinding
import net.ivpn.client.ui.serverlist.fastest.FastestSettingNavigator
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewAdapter
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewModel
import net.ivpn.client.v2.MainActivity
import javax.inject.Inject

class FastestSettingFragment: Fragment(), FastestSettingNavigator {

    lateinit var binding: FragmentFastestSettingsBinding

    @Inject
    lateinit var viewModel: FastestSettingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fastest_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        init()
        initToolbar()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(false)
            }
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun init() {
        viewModel.setNavigator(this)
        binding.contentLayout.viewmodel = viewModel

        val adapter = FastestSettingViewAdapter()
        binding.contentLayout.recyclerView.adapter = adapter
        binding.contentLayout.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun showToast() {
        ToastUtil.toast(R.string.fastest_setting_toast)
    }
}