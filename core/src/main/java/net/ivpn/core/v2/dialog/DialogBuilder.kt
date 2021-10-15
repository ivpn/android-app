package net.ivpn.core.v2.dialog

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
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.text.InputFilter
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.InputFilterMinMax
import net.ivpn.core.common.extension.setContentSecure
import net.ivpn.core.common.utils.DateUtil
import net.ivpn.core.databinding.DialogCustomDnsBinding
import net.ivpn.core.v2.customdns.OnDNSChangedListener
import net.ivpn.core.v2.protocol.dialog.WireGuardDetailsDialogListener
import net.ivpn.core.v2.protocol.dialog.WireGuardInfo
import net.ivpn.core.v2.timepicker.OnDelayOptionSelected
import net.ivpn.core.v2.timepicker.PauseDelay
import org.slf4j.LoggerFactory
import java.util.*

object DialogBuilder {
    private val LOGGER = LoggerFactory.getLogger(DialogBuilder::class.java)

    @JvmStatic
    fun createOptionDialog(
        context: Context?, dialogAttr: Dialogs,
        listener: DialogInterface.OnClickListener?
    ) {
        LOGGER.info("Create dialog $dialogAttr")
        if (context == null) {
            return
        }
        val builder = MaterialAlertDialogBuilder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(dialogAttr.titleId))
        builder.setMessage(context.getString(dialogAttr.messageId))
        if (dialogAttr.positiveBtnId != -1) {
            builder.setPositiveButton(context.getString(dialogAttr.positiveBtnId), listener)
        }
        builder.setNegativeButton(context.getString(dialogAttr.negativeBtnId), null)
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            val dialog: Dialog = builder.show()
            val messageView = dialog.window!!.findViewById<TextView>(android.R.id.message)
            messageView.setTextAppearance(context, R.style.DialogMessageStyle)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createNotificationDialog(context: Context?, dialogAttr: Dialogs?) {
        LOGGER.info("Create dialog $dialogAttr")
        if (context == null || dialogAttr == null) {
            return
        }
        val builder = MaterialAlertDialogBuilder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(dialogAttr.titleId))
        builder.setMessage(context.getString(dialogAttr.messageId))
        builder.setNegativeButton(context.getString(dialogAttr.negativeBtnId), null)
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            val dialog: Dialog = builder.show()
            val messageView = dialog.window!!.findViewById<TextView>(android.R.id.message)
            messageView.setTextAppearance(context, R.style.DialogMessageStyle)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createFullCustomNotificationDialog(context: Context?, title: String?, msg: String?) {
        LOGGER.info("Create dialog ")
        if (context == null) {
            return
        }
        val builder = MaterialAlertDialogBuilder(context, R.style.AlertDialog)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setNegativeButton(context.getString(R.string.dialogs_ok), null)
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            val dialog: Dialog = builder.show()
            val messageView = dialog.window!!.findViewById<TextView>(android.R.id.message)
            messageView.setTextAppearance(context, R.style.DialogMessageStyle)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createFullCustomNotificationDialog(
        context: Context?, title: String?, msg: String?,
        cancelListener: DialogInterface.OnCancelListener?
    ) {
        LOGGER.info("Create dialog ")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setOnCancelListener(cancelListener)
        builder.setNegativeButton(context.getString(R.string.dialogs_ok)) { dialog: DialogInterface?, _: Int ->
            cancelListener?.onCancel(
                dialog
            )
        }
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            val dialog: Dialog = builder.show()
            val messageView = dialog.window!!.findViewById<TextView>(android.R.id.message)
            messageView.setTextAppearance(context, R.style.DialogMessageStyle)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createNonCancelableDialog(
        context: Context?,
        dialogAttr: Dialogs,
        positiveAction: () -> Unit,
        cancelAction: () -> Unit
    ) : Dialog? {
        LOGGER.info("Create dialog $dialogAttr")
        if (context == null) {
            return null
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(dialogAttr.titleId))
        builder.setMessage(context.getString(dialogAttr.messageId))

        if (dialogAttr.positiveBtnId != -1) {
            builder.setPositiveButton(context.getString(dialogAttr.positiveBtnId)) { _: DialogInterface?, _: Int ->
                positiveAction()
            }
        }
        builder.setOnCancelListener {
            cancelAction()
        }
        if (dialogAttr.negativeBtnId != -1) {
            builder.setNegativeButton(context.getString(dialogAttr.negativeBtnId)) { dialog: DialogInterface?, _: Int ->
                cancelAction()
            }
        }
        if ((context as Activity).isFinishing) {
            return null
        }
        try {
            val dialog: AlertDialog = builder.show()
            dialog.findViewById<TextView>(android.R.id.message).also {
                it?.setTextAppearance(context, R.style.DialogMessageStyle)
            }
            return dialog
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return null
    }

    @JvmStatic
    fun createPredefinedTimePickerDialog(
        context: Context?,
        onDelayOptionSelected: OnDelayOptionSelected
    ) {
        LOGGER.info("Create time picker dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_predefined_time_picker, null)
        val delayMap: MutableMap<Int, PauseDelay> = HashMap()
        delayMap[R.id.first_variant] = PauseDelay.FIVE_MINUTES
        delayMap[R.id.second_variant] = PauseDelay.FIFTEEN_MINUTES
        delayMap[R.id.third_variant] = PauseDelay.ONE_HOUR
        delayMap[R.id.custom_variant] = PauseDelay.CUSTOM_DELAY
        builder.setView(dialogView)
        val alertDialog = builder.create()
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(R.id.first_variant)
        dialogView.findViewById<View>(R.id.apply_button).setOnClickListener {
            alertDialog.dismiss()
            val checkedId = radioGroup.checkedRadioButtonId
            if (checkedId != -1) {
                onDelayOptionSelected.onDelayOptionSelected(delayMap[checkedId])
            }
        }
        dialogView.findViewById<View>(R.id.cancel_button).setOnClickListener {
            alertDialog.dismiss()
            onDelayOptionSelected.onCancelAction()
        }
        alertDialog.setOnCancelListener { onDelayOptionSelected.onCancelAction() }
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            alertDialog.show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createCustomTimePickerDialog(
        context: Context?,
        onDelayOptionSelected: OnDelayOptionSelected
    ) {
        LOGGER.info("Create custom time picker dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_time_picker, null)
        val pauseTime = LongArray(1)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)
        timePicker.currentMinute = 0
        timePicker.currentHour = 0
        timePicker.setOnTimeChangedListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
            pauseTime[0] = hourOfDay * DateUtil.HOUR + minute * DateUtil.MINUTE
        }
        builder.setView(dialogView)
        val alertDialog = builder.create()
        dialogView.findViewById<View>(R.id.apply_button).setOnClickListener {
            alertDialog.dismiss()
            onDelayOptionSelected.onCustomDelaySelected(pauseTime[0])
        }
        dialogView.findViewById<View>(R.id.cancel_button).setOnClickListener {
            alertDialog.dismiss()
            onDelayOptionSelected.onCancelAction()
        }
        alertDialog.setOnCancelListener { onDelayOptionSelected.onCancelAction() }
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            alertDialog.show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createWireGuardDetailsDialog(
        context: Context?, info: WireGuardInfo,
        listener: WireGuardDetailsDialogListener
    ) {
        LOGGER.info("Create wireguard details dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_wireguard_details, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        (dialogView.findViewById<View>(R.id.wg_public_key) as TextView).text = info.publicKey
        (dialogView.findViewById<View>(R.id.wg_ip_address) as TextView).text = info.ipAddress
        (dialogView.findViewById<View>(R.id.wg_last_generated) as TextView).text =
            info.lastGenerated
        (dialogView.findViewById<View>(R.id.wg_regenerate_in) as TextView).text =
            info.nextRegenerationDate
        (dialogView.findViewById<View>(R.id.wg_valid_until) as TextView).text = info.validUntil
        dialogView.findViewById<View>(R.id.cancelAction)
            .setOnClickListener { alertDialog.dismiss() }
        dialogView.findViewById<View>(R.id.reGenerateAction).setOnClickListener {
            listener.reGenerateKeys()
            alertDialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.clipboard_copy)
            .setOnClickListener { listener.copyPublicKeyToClipboard() }
        dialogView.findViewById<View>(R.id.ip_clipboard_copy)
            .setOnClickListener { listener.copyIpAddressToClipboard() }

        if ((context as Activity).isFinishing) {
            return
        }
        try {
            alertDialog.show()
            alertDialog.window?.setContentSecure(true)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun createCustomDNSDialogue(context: Context?, listener: OnDNSChangedListener?) {
        LOGGER.info("Create connection info dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewModel =
            IVPNApplication.appComponent.provideActivityComponent().create().dialogueViewModel
        viewModel.setOnDnsChangedListener(listener)
        val binding: DialogCustomDnsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_custom_dns, null, false
        )
        binding.viewmodel = viewModel
        val dialogView = binding.root
        builder.setView(dialogView)
        val alertDialog = builder.create()
        binding.firstValue.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        binding.secondValue.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        binding.thirdValue.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        binding.forthValue.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        binding.forthValue.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (viewModel.validateDNS()) {
                    alertDialog.dismiss()
                }
            }
            false
        }
        binding.applyAction.setOnClickListener {
            if (viewModel.validateDNS()) {
                alertDialog.dismiss()
            }
        }
        binding.cancelAction.setOnClickListener { alertDialog.dismiss() }
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            alertDialog.show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}