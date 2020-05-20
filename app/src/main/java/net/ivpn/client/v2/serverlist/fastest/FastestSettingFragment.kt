package net.ivpn.client.v2.serverlist.fastest

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
import net.ivpn.client.common.utils.ToastUtil
import net.ivpn.client.databinding.FragmentFastestSettingsBinding
import net.ivpn.client.ui.serverlist.fastest.FastestSettingNavigator
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewAdapter
import net.ivpn.client.ui.serverlist.fastest.FastestSettingViewModel
import javax.inject.Inject

class FastestSettingFragment: Fragment(), FastestSettingNavigator {

    lateinit var binding: FragmentFastestSettingsBinding

    @Inject
    lateinit var viewModel: FastestSettingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fastest_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        init()
        initToolbar()
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun init() {
        viewModel.setNavigator(this)
        binding.contentLayout.viewmodel = viewModel

        val adapter = FastestSettingViewAdapter()
        binding.contentLayout.recyclerView.adapter = adapter
        binding.contentLayout.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun showToast() {
        ToastUtil.toast(R.string.fastest_setting_toast)
    }
}