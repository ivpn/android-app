package net.ivpn.client.v2.alwaysonvpn

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentAlwaysOnVpnBinding
import net.ivpn.client.ui.alwaysonvpn.AlwaysOnVpnActivity
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.v2.viewmodel.AlwaysOnVPNViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AlwaysOnVPNFragment: Fragment() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AlwaysOnVpnActivity::class.java)
    }
    private lateinit var binding: FragmentAlwaysOnVpnBinding

    @Inject
    lateinit var alwaysOnVPN: AlwaysOnVPNViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_always_on_vpn, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        alwaysOnVPN.onResume()
    }

    private fun initViews() {
        binding.contentLayout.toSettings.setOnClickListener {
            openDeviceSettings()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    @SuppressLint("InlinedApi")
    private fun openDeviceSettings() {
        LOGGER.info("Navigate to VPNs list device settings screen")
        if (alwaysOnVPN.isAlwaysOnVpnSupported.get()) {
            try {
                startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
            } catch (exception: ActivityNotFoundException) {
                exception.printStackTrace()
                LOGGER.error("Error while navigating to VPN device settings")
                DialogBuilder.createNotificationDialog(context, Dialogs.ALWAYS_ON_VPN_NOT_SUPPORTED)
            }
        }
    }
}