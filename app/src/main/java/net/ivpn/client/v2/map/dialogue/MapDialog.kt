package net.ivpn.client.v2.map.dialogue

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import net.ivpn.client.R
import net.ivpn.client.databinding.DialogueLocationBinding

object MapDialog {

    fun openDarkModeDialogue(context: Context) {
        val builder: AlertDialog.Builder =
                AlertDialog.Builder(context, R.style.AppTheme_MapDialog)
        val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding: DialogueLocationBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialogue_location, null, false
        )

        builder.setView(binding.root)
        val alertDialog = builder.create()

        if ((context as Activity).isFinishing) {
            return
        }

        val params = alertDialog.window.attributes

        alertDialog.setOnShowListener {
            alertDialog.window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        params.y = -200
        alertDialog.show()
    }
}