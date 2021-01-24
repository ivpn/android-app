package net.ivpn.client.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.InputFilterMinMax
import net.ivpn.client.common.extension.setContentSecure
import net.ivpn.client.common.utils.DateUtil
import net.ivpn.client.databinding.DialogCustomDnsBinding
import net.ivpn.client.ui.customdns.OnDNSChangedListener
import net.ivpn.client.ui.protocol.dialog.WireGuardDetailsDialogListener
import net.ivpn.client.ui.protocol.dialog.WireGuardDialogInfo
import net.ivpn.client.ui.settings.AdvancedKillSwitchActionListener
import net.ivpn.client.ui.timepicker.OnDelayOptionSelected
import net.ivpn.client.ui.timepicker.PauseDelay
import org.slf4j.LoggerFactory
import java.util.*

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

object DialogBuilder {
    private val LOGGER = LoggerFactory.getLogger(DialogBuilder::class.java)
    fun createOptionDialog(context: Context?, dialogAttr: Dialogs,
                           listener: DialogInterface.OnClickListener?) {
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
    fun createFullCustomNotificationDialog(context: Context?, title: String?, msg: String?,
                                           cancelListener: DialogInterface.OnCancelListener?) {
        LOGGER.info("Create dialog ")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setOnCancelListener(cancelListener)
        builder.setNegativeButton(context.getString(R.string.dialogs_ok)) { dialog: DialogInterface?, _: Int -> cancelListener?.onCancel(dialog) }
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

    fun createNonCancelableDialog(context: Context?, dialogAttr: Dialogs, listener: DialogInterface.OnClickListener?,
                                  cancelListener: DialogInterface.OnCancelListener?) {
        LOGGER.info("Create dialog $dialogAttr")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(dialogAttr.titleId))
        builder.setMessage(context.getString(dialogAttr.messageId))
        if (dialogAttr.positiveBtnId != -1) {
            builder.setPositiveButton(context.getString(dialogAttr.positiveBtnId), listener)
        }
        builder.setOnCancelListener(cancelListener)
        if (dialogAttr.negativeBtnId != -1) {
            builder.setNegativeButton(context.getString(dialogAttr.negativeBtnId)) { dialog: DialogInterface?, which: Int -> cancelListener?.onCancel(dialog) }
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
    fun createPredefinedTimePickerDialog(context: Context?,
                                         onDelayOptionSelected: OnDelayOptionSelected) {
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
        dialogView.findViewById<View>(R.id.apply_button).setOnClickListener { view: View? ->
            alertDialog.dismiss()
            val checkedId = radioGroup.checkedRadioButtonId
            if (checkedId != -1) {
                onDelayOptionSelected.onDelayOptionSelected(delayMap[checkedId])
            }
        }
        dialogView.findViewById<View>(R.id.cancel_button).setOnClickListener { view: View? ->
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
    fun createCustomTimePickerDialog(context: Context?,
                                     onDelayOptionSelected: OnDelayOptionSelected) {
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
        timePicker.setOnTimeChangedListener { view: TimePicker?, hourOfDay: Int, minute: Int -> pauseTime[0] = hourOfDay * DateUtil.HOUR + minute * DateUtil.MINUTE }
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

    fun createAdvancedKillSwitchDialog(context: Context?, listener: AdvancedKillSwitchActionListener) {
        LOGGER.info("Create advanced killswitch dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_advanced_kill_switch, null)
        builder.setView(dialogView)
        val checkBox: AppCompatCheckBox = dialogView.findViewById(R.id.checkbox)
        val alertDialog = builder.create()
        dialogView.findViewById<View>(R.id.cancelAction).setOnClickListener {
            if (checkBox.isChecked) {
                listener.enableAdvancedKillSwitchDialog(false)
            }
            alertDialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.openSettings).setOnClickListener { listener.openDeviceSettings() }
        alertDialog.setOnCancelListener {
            if (checkBox.isChecked) {
                listener.enableAdvancedKillSwitchDialog(false)
            }
            alertDialog.dismiss()
        }
        if ((context as Activity).isFinishing) {
            return
        }
        try {
            alertDialog.show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun createWireGuardDetailsDialog(context: Context?, info: WireGuardDialogInfo,
                                     listener: WireGuardDetailsDialogListener) {
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
        (dialogView.findViewById<View>(R.id.wg_last_generated) as TextView).text = info.lastGenerated
        (dialogView.findViewById<View>(R.id.wg_regenerate_in) as TextView).text = info.nextRegenerationDate
        (dialogView.findViewById<View>(R.id.wg_valid_until) as TextView).text = info.validUntil
        dialogView.findViewById<View>(R.id.cancelAction).setOnClickListener { alertDialog.dismiss() }
        dialogView.findViewById<View>(R.id.reGenerateAction).setOnClickListener {
            listener.reGenerateKeys()
            alertDialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.clipboard_copy).setOnClickListener { listener.copyPublicKeyToClipboard() }
        dialogView.findViewById<View>(R.id.ip_clipboard_copy).setOnClickListener { listener.copyIpAddressToClipboard() }

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

    fun createCustomDNSDialogue(context: Context?, listener: OnDNSChangedListener?) {
        LOGGER.info("Create connection info dialog")
        if (context == null) {
            return
        }
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewModel = IVPNApplication.getApplication().appComponent.provideActivityComponent().create().dialogueViewModel
        viewModel.setOnDnsChangedListener(listener)
        val binding: DialogCustomDnsBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_custom_dns, null, false)
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