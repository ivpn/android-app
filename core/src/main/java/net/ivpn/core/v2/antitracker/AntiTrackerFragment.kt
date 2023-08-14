package net.ivpn.core.v2.antitracker

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.
 
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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.databinding.FragmentAntitrackerBinding
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.viewmodel.AntiTrackerViewModel
import javax.inject.Inject

class AntiTrackerFragment: Fragment() {

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    lateinit var binding: FragmentAntitrackerBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_antitracker, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
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

    override fun onResume() {
        super.onResume()
        antiTracker.reset()
    }

    private fun initViews() {
        binding.contentLayout.antitracker = antiTracker
        binding.contentLayout.readMoreAntitracker.setOnClickListener {
            readMore()
        }
        binding.contentLayout.readMoreBlockList.setOnClickListener {
            readMoreBlockList()
        }
        binding.contentLayout.readMoreHardcore.setOnClickListener {
            readMoreHardcore()
        }
        binding.contentLayout.changeAntiTracker.setOnClickListener {
            val action = AntiTrackerFragmentDirections.actionAntiTrackerFragmentToAntiTrackerListFragment()
            navigate(action)
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun readMore() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.ivpn.net/antitracker/")
        startActivity(openURL)
    }

    private fun readMoreBlockList() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.ivpn.net/knowledgebase/general/antitracker-plus-lists-explained/")
        startActivity(openURL)
    }

    private fun readMoreHardcore() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.ivpn.net/knowledgebase/general/antitracker-faq/")
        startActivity(openURL)
    }
}