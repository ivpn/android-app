package net.ivpn.core.v2.protocol.port

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.databinding.FragmentCustomPortBinding
import net.ivpn.core.ui.theme.AppTheme
import javax.inject.Inject

@ApplicationScope
class CustomPortFragment : Fragment() {

    @Inject
    lateinit var viewModel: CustomPortViewModel

    lateinit var binding: FragmentCustomPortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_custom_port, container, false)
        return binding.root.apply {
            findViewById<ComposeView>(R.id.view_custom_port).setContent {
                AppTheme {
                    CustomPortScreen(navController = findNavController(), viewModel = viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

}
