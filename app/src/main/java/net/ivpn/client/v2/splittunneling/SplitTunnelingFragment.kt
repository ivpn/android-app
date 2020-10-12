package net.ivpn.client.v2.splittunneling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentSplitTunnelingBinding
import net.ivpn.client.ui.split.SplitTunnelingViewModel
import javax.inject.Inject

class SplitTunnelingFragment : Fragment() {

    @Inject
    lateinit var viewModel: SplitTunnelingViewModel

    private lateinit var binding : FragmentSplitTunnelingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_split_tunneling, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        init()
    }

    private fun init() {
        binding.viewmodel = viewModel
        binding.contentLayout.viewmodel = viewModel
        binding.contentLayout.recyclerView.layoutManager = LinearLayoutManager(context)

        getAllApplications()
    }

    private fun getAllApplications() {
        viewModel.getApplicationsList(context!!.packageManager)
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}