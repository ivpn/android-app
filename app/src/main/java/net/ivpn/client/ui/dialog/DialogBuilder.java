package net.ivpn.client.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.InputFilterMinMax;
import net.ivpn.client.common.utils.DateUtil;
import net.ivpn.client.databinding.DialogCustomDnsBinding;
import net.ivpn.client.ui.customdns.DialogueCustomDNSViewModel;
import net.ivpn.client.ui.customdns.OnDNSChangedListener;
import net.ivpn.client.ui.protocol.dialog.WireGuardDetailsDialogListener;
import net.ivpn.client.ui.protocol.dialog.WireGuardDialogInfo;
import net.ivpn.client.ui.settings.AdvancedKillSwitchActionListener;
import net.ivpn.client.ui.timepicker.OnDelayOptionSelected;
import net.ivpn.client.ui.timepicker.PauseDelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DialogBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DialogBuilder.class);

    public static void createOptionDialog(Context context, Dialogs dialogAttr,
                                          DialogInterface.OnClickListener listener) {
        LOGGER.info("Create dialog " + dialogAttr);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialog);
        builder.setTitle(context.getString(dialogAttr.getTitleId()));
        builder.setMessage(context.getString(dialogAttr.getMessageId()));
        if (dialogAttr.getPositiveBtnId() != -1) {
            builder.setPositiveButton(context.getString(dialogAttr.getPositiveBtnId()), listener);
        }
        builder.setNegativeButton(context.getString(dialogAttr.getNegativeBtnId()), null);
        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            Dialog dialog = builder.show();

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTextAppearance(context, R.style.DialogMessageStyle);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createNotificationDialog(Context context, Dialogs dialogAttr) {
        LOGGER.info("Create dialog " + dialogAttr);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialog);
        builder.setTitle(context.getString(dialogAttr.getTitleId()));
        builder.setMessage(context.getString(dialogAttr.getMessageId()));
        builder.setNegativeButton(context.getString(dialogAttr.getNegativeBtnId()), null);
        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            Dialog dialog = builder.show();

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTextAppearance(context, R.style.DialogMessageStyle);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createFullCustomNotificationDialog(Context context, String title, String msg) {
        LOGGER.info("Create dialog ");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialog);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNegativeButton(context.getString(R.string.dialogs_ok), null);
        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            Dialog dialog = builder.show();

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTextAppearance(context, R.style.DialogMessageStyle);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createFullCustomNotificationDialog(Context context, String title, String msg,
                                                          final DialogInterface.OnCancelListener cancelListener) {
        LOGGER.info("Create dialog ");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setOnCancelListener(cancelListener);
            builder.setNegativeButton(context.getString(R.string.dialogs_ok), (dialog, which) -> {
                if (cancelListener != null) {
                    cancelListener.onCancel(dialog);
                }
            });
        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            Dialog dialog = builder.show();

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTextAppearance(context, R.style.DialogMessageStyle);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createNonCancelableDialog(Context context, Dialogs dialogAttr, DialogInterface.OnClickListener listener,
                                                 final DialogInterface.OnCancelListener cancelListener) {
        LOGGER.info("Create dialog " + dialogAttr);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        builder.setTitle(context.getString(dialogAttr.getTitleId()));
        builder.setMessage(context.getString(dialogAttr.getMessageId()));
        if (dialogAttr.getPositiveBtnId() != -1) {
            builder.setPositiveButton(context.getString(dialogAttr.getPositiveBtnId()), listener);
        }
        builder.setOnCancelListener(cancelListener);
        if (dialogAttr.getNegativeBtnId() != -1) {
            builder.setNegativeButton(context.getString(dialogAttr.getNegativeBtnId()), (dialog, which) -> {
                if (cancelListener != null) {
                    cancelListener.onCancel(dialog);
                }
            });
        }
        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            Dialog dialog = builder.show();

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTextAppearance(context, R.style.DialogMessageStyle);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createPredefinedTimePickerDialog(Context context,
                                                        final OnDelayOptionSelected onDelayOptionSelected) {
        LOGGER.info("Create time picker dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_predefined_time_picker, null);
        final Map<Integer, PauseDelay> delayMap = new HashMap<>();
        delayMap.put(R.id.first_variant, PauseDelay.FIVE_MINUTES);
        delayMap.put(R.id.second_variant, PauseDelay.FIFTEEN_MINUTES);
        delayMap.put(R.id.third_variant, PauseDelay.ONE_HOUR);
        delayMap.put(R.id.custom_variant, PauseDelay.CUSTOM_DELAY);

        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        final RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);
        radioGroup.check(R.id.first_variant);
        dialogView.findViewById(R.id.apply_button).setOnClickListener(view -> {
            alertDialog.dismiss();
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId != -1) {
                onDelayOptionSelected.onDelayOptionSelected(delayMap.get(checkedId));
            }
        });
        dialogView.findViewById(R.id.cancel_button).setOnClickListener(view -> {
            alertDialog.dismiss();
            onDelayOptionSelected.onCancelAction();
        });

        alertDialog.setOnCancelListener(dialogInterface -> {
            onDelayOptionSelected.onCancelAction();
//                listener.onWatchedFeatureInfo();
        });

        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            alertDialog.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createCustomTimePickerDialog(Context context,
                                                    final OnDelayOptionSelected onDelayOptionSelected) {
        LOGGER.info("Create custom time picker dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_custom_time_picker, null);
        final long[] pauseTime = new long[1];
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentMinute(0);
        timePicker.setCurrentHour(0);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> pauseTime[0] = hourOfDay * DateUtil.HOUR + minute * DateUtil.MINUTE);

        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        dialogView.findViewById(R.id.apply_button).setOnClickListener(view -> {
            alertDialog.dismiss();
            onDelayOptionSelected.onCustomDelaySelected(pauseTime[0]);
        });
        dialogView.findViewById(R.id.cancel_button).setOnClickListener(view -> {
            alertDialog.dismiss();
            onDelayOptionSelected.onCancelAction();
        });

        alertDialog.setOnCancelListener(dialogInterface -> onDelayOptionSelected.onCancelAction());

        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            alertDialog.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createAdvancedKillSwitchDialog(Context context, final AdvancedKillSwitchActionListener listener) {
        LOGGER.info("Create advanced killswitch dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;
        View dialogView = inflater.inflate(R.layout.dialog_advanced_kill_switch, null);

        builder.setView(dialogView);

        final AppCompatCheckBox checkBox = dialogView.findViewById(R.id.checkbox);
        final AlertDialog alertDialog = builder.create();
        dialogView.findViewById(R.id.cancelAction).setOnClickListener(view -> {
            if (checkBox.isChecked()) {
                listener.enableAdvancedKillSwitchDialog(false);
            }
            alertDialog.dismiss();
        });

        dialogView.findViewById(R.id.openSettings).setOnClickListener(view -> listener.openDeviceSettings());

        alertDialog.setOnCancelListener(dialogInterface -> {
            if (checkBox.isChecked()) {
                listener.enableAdvancedKillSwitchDialog(false);
            }
            alertDialog.dismiss();
        });

        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            alertDialog.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createWireGuardDetailsDialog(Context context, WireGuardDialogInfo info,
                                                    final WireGuardDetailsDialogListener listener) {
        LOGGER.info("Create wireguard details dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;
        View dialogView = inflater.inflate(R.layout.dialog_wireguard_details, null);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        ((TextView) dialogView.findViewById(R.id.wg_public_key)).setText(info.getPublicKey());
        ((TextView) dialogView.findViewById(R.id.wg_ip_address)).setText(info.getIpAddress());
        ((TextView) dialogView.findViewById(R.id.wg_last_generated)).setText(info.getLastGenerated());
        ((TextView) dialogView.findViewById(R.id.wg_regenerate_in)).setText(info.getNextRegenerationDate());
        ((TextView) dialogView.findViewById(R.id.wg_valid_until)).setText(info.getValidUntil());
        dialogView.findViewById(R.id.cancelAction).setOnClickListener(view -> alertDialog.dismiss());
        dialogView.findViewById(R.id.reGenerateAction).setOnClickListener(view -> {
            listener.reGenerateKeys();
            alertDialog.dismiss();
        });
        dialogView.findViewById(R.id.clipboard_copy).setOnClickListener(view -> listener.copyPublicKeyToClipboard());
        dialogView.findViewById(R.id.ip_clipboard_copy).setOnClickListener(view -> listener.copyIpAddressToClipboard());

        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            alertDialog.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void createCustomDNSDialogue(Context context, OnDNSChangedListener listener) {
        LOGGER.info("Create connection info dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;

        DialogueCustomDNSViewModel viewModel =
                IVPNApplication.getApplication().appComponent.provideActivityComponent().create().getDialogueViewModel();
        viewModel.setOnDnsChangedListener(listener);

        DialogCustomDnsBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_custom_dns, null, false);
        binding.setViewmodel(viewModel);
        View dialogView = binding.getRoot();
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        binding.firstValue.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
        binding.secondValue.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
        binding.thirdValue.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
        binding.forthValue.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
        binding.forthValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (viewModel.validateDNS()) {
                    alertDialog.dismiss();
                }
            }
            return false;
        });

        binding.applyAction.setOnClickListener(view -> {
            if (viewModel.validateDNS()) {
                alertDialog.dismiss();
            }
        });
        binding.cancelAction.setOnClickListener(view -> alertDialog.dismiss());

        if (((Activity) context).isFinishing()) {
            return;
        }
        try {
            alertDialog.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}