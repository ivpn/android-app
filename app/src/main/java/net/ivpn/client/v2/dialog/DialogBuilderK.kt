package net.ivpn.client.v2.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import net.ivpn.client.R
import net.ivpn.client.common.nightmode.OnNightModeChangedListener
import net.ivpn.client.databinding.DialogueDefaultNetworkStateBinding
import net.ivpn.client.databinding.DialogueFilterBinding
import net.ivpn.client.databinding.DialogueNetworkStateBinding
import net.ivpn.client.databinding.DialogueNightModeBinding
import net.ivpn.client.ui.network.CommonBehaviourItemViewModel
import net.ivpn.client.ui.network.NetworkItemViewModel
import net.ivpn.client.v2.viewmodel.ColorThemeViewModel
import net.ivpn.client.v2.viewmodel.ServerListFilterViewModel

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

    fun openChangeNetworkStatusDialogue(context: Context, wifiItemViewModel: NetworkItemViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueNetworkStateBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_network_state, null, false
        )

        binding.network = wifiItemViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            wifiItemViewModel.applyState()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
    }

    fun openChangeDefaultNetworkStatusDialogue(context: Context, defaultViewModel: CommonBehaviourItemViewModel) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueDefaultNetworkStateBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_default_network_state, null, false
        )

        binding.network = defaultViewModel
        builder.setView(binding.root)
        val alertDialog = builder.create()
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.applyButton.setOnClickListener {
            alertDialog.dismiss()
            defaultViewModel.applyState()
        }

        if ((context as Activity).isFinishing) {
            return
        }

        alertDialog.show()
    }
}