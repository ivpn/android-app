package net.ivpn.client.common.utils

import android.content.Intent
import android.net.Uri

object IntentUtils {

    private const val URL = "https://www.ivpn.net/signup/IVPN%20Pro/Annually"

    fun createWebSignUpIntent(): Intent {
        val webPage = Uri.parse(URL)

        return Intent(Intent.ACTION_VIEW, webPage)
    }
}