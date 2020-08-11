package net.ivpn.client.v2.customdns

import android.os.Bundle
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
import net.ivpn.client.databinding.FragmentCustomDnsBinding
import net.ivpn.client.ui.customdns.CustomDNSViewModel
import net.ivpn.client.ui.dialog.DialogBuilder
import org.slf4j.LoggerFactory
import javax.inject.Inject

class CustomDNSFragment : Fragment() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CustomDNSFragment::class.java)
    }

    lateinit var binding: FragmentCustomDnsBinding

    @Inject
    lateinit var viewModel: CustomDNSViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_custom_dns, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel

        binding.contentLayout.changeDnsButton.setOnClickListener {
            changeDNS()
        }
    }

    fun changeDNS() {
        DialogBuilder.createCustomDNSDialogue(context) { dns: String? -> viewModel.setDnsAs(dns) }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}