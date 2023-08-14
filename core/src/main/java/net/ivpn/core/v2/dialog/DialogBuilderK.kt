package net.ivpn.core.v2.dialog

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

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import net.ivpn.core.R
import net.ivpn.core.common.nightmode.OnNightModeChangedListener
import net.ivpn.core.databinding.DialogueDefaultNetworkStateBinding
import net.ivpn.core.databinding.DialogueFilterBinding
import net.ivpn.core.databinding.DialogueNetworkStateBinding
import net.ivpn.core.databinding.DialogueNightModeBinding
import net.ivpn.core.v2.network.dialog.NetworkChangeDialogViewModel
import net.ivpn.core.v2.viewmodel.ColorThemeViewModel
import net.ivpn.core.v2.viewmodel.ServerListFilterViewModel

object DialogBuilderK {

    fun openDarkModeDialogue(context: Context, listener: OnNightModeChangedListener, colorThemeViewModel: ColorThemeViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueNightModeBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_night_mode, null, false
        )

        binding.colorTheme = colorThemeViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
            listener.onNightModeCancelClicked()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            colorThemeViewModel.applyMode()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
        alertDialog.setOnCancelListener { listener.onNightModeCancelClicked() }
    }

    fun openSortServerDialogue(
            context: Context,
            listener: ServerListFilterViewModel.OnFilterChangedListener,
            filterViewModel: ServerListFilterViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueFilterBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_filter, null, false
        )

        binding.filter = filterViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            filterViewModel.applyMode()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
    }

    fun openChangeNetworkStatusDialogue(context: Context, dialogViewModel: NetworkChangeDialogViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueNetworkStateBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_network_state, null, false
        )

        binding.viewmodel = dialogViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            dialogViewModel.apply()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
    }

    fun openChangeDefaultNetworkStatusDialogue(context: Context, dialogViewModel: NetworkChangeDialogViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueDefaultNetworkStateBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_default_network_state, null, false
        )

        binding.network = dialogViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            dialogViewModel.apply()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
    }
}