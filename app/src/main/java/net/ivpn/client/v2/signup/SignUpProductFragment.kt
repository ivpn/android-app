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
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.databinding.FragmentSignUpProductBinding
import net.ivpn.client.v2.viewmodel.SignUpViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class SignUpProductFragment : Fragment() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SignUpProductFragment::class.java)
    }

    lateinit var binding: FragmentSignUpProductBinding

    @Inject
    lateinit var viewModel: SignUpViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_product, container, false)
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

        binding.contentLayout.standardPlanButton.setOnClickListener {
            viewModel.selectedPlan.set(Plan.STANDARD)
            openAddFundAccount()
        }
        binding.contentLayout.proPlanButton.setOnClickListener {
            viewModel.selectedPlan.set(Plan.PRO)
            openAddFundAccount()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openAddFundAccount() {
        val action = SignUpProductFragmentDirections.actionSignUpFragmentToSignUpPeriodFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }
}