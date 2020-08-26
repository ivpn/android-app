package net.ivpn.client.v2.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentSyncBinding
import net.ivpn.client.ui.syncservers.SyncServersNavigator
import net.ivpn.client.ui.syncservers.SyncServersViewModel
import net.ivpn.client.v2.login.LoginFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SyncFragment: Fragment(), SyncServersNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SyncFragment::class.java)
    }

    lateinit var binding: FragmentSyncBinding

    @Inject
    lateinit var viewModel: SyncServersViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncServers()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }

    private fun initViews() {
        viewModel.setNavigator(this)
        binding.viewmodel = viewModel
    }

    override fun onGetServers() {
        LOGGER.info("Servers information was updated")
//        val action = SyncFragmentDirections.actionSyncFragmentToConnectFragment()
//        NavHostFragment.findNavController(this).navigate(action)
//        NavHostFragment.findNavController(this).popBackStack(R.id.connectFragment, false)
        NavHostFragment.findNavController(this).popBackStack()
    }
}