package net.ivpn.client.v2.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import net.ivpn.client.R
import net.ivpn.client.common.nightmode.OnNightModeChangedListener
import net.ivpn.client.databinding.DialogueNightModeBinding
import net.ivpn.client.v2.viewmodel.ThemeViewModel

object DialogBuilderK {

    //Move listener to ThemeViewModel
    //Maybe colorThemeViewModel should be gotten from AppComponent
    fun openDarkModeDialogue(context: Context, listener: OnNightModeChangedListener, colorThemeViewModel: ThemeViewModel) {
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
}