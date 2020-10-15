package net.ivpn.client.common.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.VpnService
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs

private fun Fragment.openErrorDialog(dialogs: Dialogs) {
    DialogBuilder.createNotificationDialog(context, dialogs)
}

fun Fragment.checkVPNPermission(requestCode: Int) {
    val intent: Intent?
    intent = try {
        VpnService.prepare(context)
    } catch (exception: Exception) {
        exception.printStackTrace()
        openErrorDialog(Dialogs.FIRMWARE_ERROR)
        return
    }
    if (intent != null) {
        try {
            startActivityForResult(intent, requestCode)
        } catch (ane: ActivityNotFoundException) {
        }
    } else {
        onActivityResult(requestCode, Activity.RESULT_OK, null)
    }
}

fun Fragment.findNavControllerSafely(): NavController? {
    return if (isAdded) {
        findNavController()
    } else {
        null
    }
}