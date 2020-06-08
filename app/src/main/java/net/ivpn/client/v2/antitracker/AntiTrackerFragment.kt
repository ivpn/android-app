package net.ivpn.client.v2.antitracker

import android.content.Intent
import android.net.Uri
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
import net.ivpn.client.databinding.FragmentAntitrackerBinding
import net.ivpn.client.v2.viewmodel.AntiTrackerViewModel
import javax.inject.Inject

class AntiTrackerFragment: Fragment() {

    @Inject
    lateinit var antiTracker: AntiTrackerViewModel

    lateinit var binding: FragmentAntitrackerBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_antitracker, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    private fun initViews() {
        binding.contentLayout.antitracker = antiTracker
        binding.contentLayout.readMoreAntitracker.setOnClickListener {
            readMore()
        }
        binding.contentLayout.readMoreHardcore.setOnClickListener {
            readMoreHardcore()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun readMore() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.ivpn.net/antitracker")
        startActivity(openURL)
    }

    private fun readMoreHardcore() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.ivpn.net/antitracker/hardcore")
        startActivity(openURL)
    }
}