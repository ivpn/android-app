package net.ivpn.client.v2.network

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.ivpn.client.BuildConfig
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentNetworkBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.network.NetworkNavigator
import net.ivpn.client.v2.settings.SettingsFragmentDirections
import org.slf4j.LoggerFactory
import javax.inject.Inject

class NetworkProtectionFragment : Fragment(), NetworkNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(NetworkProtectionFragment::class.java)
        private const val LOCATION_PERMISSION_CODE = 132
    }

    private lateinit var binding: FragmentNetworkBinding
    private var adapter: NetworkRecyclerViewAdapter? = null

    @Inject
    lateinit var network: NetworkViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_network, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isEmpty()) {
                    return
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    network.applyNetworkFeatureState(true)
                    return
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    network.applyNetworkFeatureState(false)
                    return
                }
            }
        }
    }

    override fun onResume() {
        LOGGER.info("onResume")
        super.onResume()
        checkLocationPermission()
    }

    private fun initViews() {
        network.setNavigator(this)
        adapter = NetworkRecyclerViewAdapter(activity)
        binding.contentLayout.viewmodel = network
        binding.contentLayout.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.contentLayout.recyclerView.adapter = adapter
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun goToAndroidAppSettings() {
        LOGGER.info("goToAndroidAppSettings")
        val action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.parse(getString(R.string.settings_package) + BuildConfig.APPLICATION_ID)
        val intent = Intent(action, uri)
        startActivity(intent)
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return
        }
        val isPermissionGranted: Boolean = isPermissionGranted()
        val isEnabled: Boolean = network.isNetworkFeatureEnabled.get()
        if (!isEnabled) {
            return
        }
        if (isPermissionGranted) {
            network.applyNetworkFeatureState(true)
            return
        }
        askPermissionRationale()
    }

    override fun toRules() {
        openNetworkProtectionRulesScreen()
    }

    override fun shouldAskForLocationPermission(): Boolean {
        LOGGER.info("shouldAskForLocationPermission")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return false
        }
        if (isPermissionGranted()) {
            return false
        }
        if (shouldRequestRationale()) {
            askPermissionRationale()
        } else {
            showInformationDialog()
        }
        return true
    }

    private fun askPermissionRationale() {
        LOGGER.info("askPermissionRationale")
        DialogBuilder.createNonCancelableDialog(activity!!, Dialogs.ASK_LOCATION_PERMISSION,
                { _: DialogInterface?, _: Int -> goToAndroidAppSettings() },
                { network.applyNetworkFeatureState(false) })
    }

    private fun showInformationDialog() {
        LOGGER.info("showInformationDialog")
        DialogBuilder.createNonCancelableDialog(activity!!, Dialogs.LOCATION_PERMISSION_INFO,
                null, DialogInterface.OnCancelListener { askPermission() })
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun shouldRequestRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_CODE)
    }

    private fun openNetworkProtectionRulesScreen() {
        val action = NetworkProtectionFragmentDirections.actionNetworkProtectionFragmentToNetworkProtectionRulesFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }
}