package net.ivpn.client.v2.login

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.common.extension.getNavigationResultBoolean
import net.ivpn.client.common.extension.navigate
import net.ivpn.client.databinding.FragmentLoginBinding
import net.ivpn.client.ui.connect.CreateSessionFragment
import net.ivpn.client.ui.connect.CreateSessionNavigator
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.login.LoginNavigator
import net.ivpn.client.v2.MainActivity
import net.ivpn.client.v2.qr.QRActivity
import net.ivpn.client.v2.viewmodel.SignUpViewModel
import net.ivpn.client.v2.viewmodel.SignUpViewModel.CreateAccountNavigator
import org.slf4j.LoggerFactory
import javax.inject.Inject

class LoginFragment : Fragment(), LoginNavigator, CreateSessionNavigator, CreateAccountNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoginFragment::class.java)
    }

    lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var signUp: SignUpViewModel

    private var createSessionFragment: CreateSessionFragment? = null

    private var originalMode: Int? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        LOGGER.info("onAttach original mode = $originalMode")
    }

    override fun onDetach() {
        super.onDetach()
        LOGGER.info("onDetach original mode = $originalMode")
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (it is MainActivity) {
                it.setAdjustResizeMode()
                it.setContentSecure(true)
            }
        }
        viewModel.navigator = this
        viewModel.reset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("OnActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        val scanningResult: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanningResult != null && scanningResult.contents != null) {
            viewModel.username.set(scanningResult.contents.toString())
            viewModel.login(false)
        } else {
            Toast.makeText(context, "Nothing scanned", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        binding.contentLayout.viewmodel = viewModel
        binding.contentLayout.signUp = signUp
        signUp.creationNavigator = this

        binding.contentLayout.editText.onFocusChangeListener = viewModel.loginFocusListener
        binding.contentLayout.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.login(false)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.contentLayout.loginButton.setOnClickListener {
            viewModel.login(false)
        }
        binding.contentLayout.signUpButton.setOnClickListener {
            createBlankAccount()
        }
        binding.contentLayout.qrCode.setOnClickListener {
            openQRScanner()
        }

        getNavigationResultBoolean("session_limit_dialogue")?.observe(viewLifecycleOwner) {
            if (it) {
                openSessionLimitReachedDialogue()
            }
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun openQRScanner() {
        val scanIntegrator = IntentIntegrator.forSupportFragment(this)
        with(scanIntegrator) {
            captureActivity = QRActivity::class.java
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Place a QR code inside viewfinder to scan Account Id.")
            initiateScan()
        }
    }

    private fun createBlankAccount() {
        signUp.blankAccountID.get()?.let { accountID ->
            if (accountID.isEmpty() || !signUp.isBlankAccountFresh()) {
                signUp.createNewAccount()
            } else {
                onAccountCreationSuccess()
            }
        } ?: kotlin.run {
            signUp.createNewAccount()
        }
    }

    override fun openCustomErrorDialogue(title: String, message: String) {
        DialogBuilder.createFullCustomNotificationDialog(context, title, message)
    }

    override fun openTFAScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToTFAFragment()
        navigate(action)
    }

    override fun openErrorDialogue(dialog: Dialogs) {
        DialogBuilder.createNotificationDialog(context, dialog)
    }

    override fun openSessionLimitReachedDialogue() {
        if (!isAdded) {
            return
        }
        createSessionFragment = CreateSessionFragment()
        createSessionFragment?.let {
            it.show(childFragmentManager, it.tag)
        }
    }

    override fun openCaptcha() {
        val action = LoginFragmentDirections.actionLoginFragmentToCaptchaFragment()
        navigate(action)
    }

    override fun onLogin() {
        val action = LoginFragmentDirections.actionLoginFragmentToSyncFragment()
        navigate(action)
    }

    override fun onLoginWithBlankAccount() {
        signUp.blankAccountID.set(viewModel.username.get())

        val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
        navigate(action)
    }

    override fun onLoginWithInactiveAccount() {
        if (viewModel.isAccountNewStyle()) {
            signUp.selectedPlan.set(Plan.getPlanByProductName(viewModel.getAccountType()))

            val action = LoginFragmentDirections.actionLoginFragmentToSignUpPeriodFragment()
            navigate(action)
        } else {
            onLogin()
        }
    }

    override fun onInvalidAccount() {
        //Nothing to do
    }

    override fun onForceLogout() {
        viewModel.login(true)
        createSessionFragment?.dismissAllowingStateLoss()
    }

    override fun tryAgain() {
        viewModel.login(false)
        createSessionFragment?.dismissAllowingStateLoss()
    }

    override fun cancel() {
        createSessionFragment?.dismissAllowingStateLoss()
    }

    override fun onAccountCreationSuccess() {
        val action = LoginFragmentDirections.actionLoginFragmentToSignUpFinishFragment()
        navigate(action)
    }

    override fun onAccountCreationError() {
        DialogBuilder.createNotificationDialog(context, Dialogs.CREATE_ACCOUNT_ERROR)
    }
}