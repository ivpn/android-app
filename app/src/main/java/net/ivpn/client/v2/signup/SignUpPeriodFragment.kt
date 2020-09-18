package net.ivpn.client.v2.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentSignUpPeriodBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.v2.viewmodel.SignUpViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SignUpPeriodFragment : Fragment(), SignUpViewModel.SignUpNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SignUpPeriodFragment::class.java)
    }

    lateinit var binding: FragmentSignUpPeriodBinding

    @Inject
    lateinit var viewModel: SignUpViewModel

    private var isAccountCreated = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_period, container, false)
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
        if (isAccountCreated) {
            val action = SignUpPeriodFragmentDirections.actionSignUpPeriodFragmentToAccountFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.navigator = this
        viewModel.initOffers()
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel

        binding.contentLayout.changeButton.setOnClickListener {
            stepBack()
        }
        binding.contentLayout.continuePurchase.setOnClickListener {
            activity?.let {
                viewModel.purchase(it)
            }
        }

    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun stepBack() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    override fun onCreateAccountFinish() {
        isAccountCreated = true
//        val action = SignUpPeriodFragmentDirections.actionSignUpPeriodFragmentToAccountFragment()
//        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onAddFundsFinish() {
        isAccountCreated = true
//        val action = SignUpPeriodFragmentDirections.actionSignUpPeriodFragmentToAccountFragment()
//        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onGoogleConnectFailure() {
        if (activity != null) {
            DialogBuilder.createFullCustomNotificationDialog(activity, getString(R.string.dialogs_error),
                    getString(R.string.billing_error_message)) {
                NavHostFragment.findNavController(this).popBackStack()
            }
        }
    }
}