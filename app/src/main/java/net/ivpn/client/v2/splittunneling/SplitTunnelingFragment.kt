package net.ivpn.client.v2.splittunneling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.todtenkopf.mvvm.MenuCommandBindings
import com.todtenkopf.mvvm.ViewModelBase
import com.todtenkopf.mvvm.ViewModelFragment
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentSplitTunnelingBinding
import net.ivpn.client.ui.split.SplitTunnelingViewModel
import javax.inject.Inject

class SplitTunnelingFragment : ViewModelFragment() {

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

    override fun createViewModel(): ViewModelBase {
        addMenuBinding(R.id.action_select_all, viewModel.selectAllCommand, MenuCommandBindings.EnableBinding.Visible)
        addMenuBinding(R.id.action_deselect_all, viewModel.deselectAllCommand, MenuCommandBindings.EnableBinding.Visible)
        return viewModel
    }

    private fun init() {
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