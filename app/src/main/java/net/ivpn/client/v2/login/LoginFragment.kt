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

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.common.extension.findNavControllerSafely
import net.ivpn.client.databinding.FragmentLoginBinding
import net.ivpn.client.ui.connect.CreateSessionFragment
import net.ivpn.client.ui.connect.CreateSessionNavigator
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.login.LoginNavigator
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

    private var originalMode : Int? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onStart() {
        super.onStart()
        originalMode = activity?.window?.getSoftInputMode()
        activity?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun onStop() {
        super.onStop()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
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
        viewModel.navigator = this
        signUp.creationNavigator = this

        binding.contentLayout.inputView.setOnEditorActionListener { _, actionId, _ ->
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
        binding.contentLayout.outlinedTextField.setEndIconOnClickListener {
            openQRScanner()
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
            if (accountID.isEmpty()  || !signUp.isBlankAccountFresh()) {
                signUp.createNewAccount()
            } else {
                onAccountCreationSuccess()
            }
        } ?: kotlin.run {
            signUp.createNewAccount()
        }
    }

    override fun openSite() {
        LOGGER.info("openSite")
        onLogin()
    }

    override fun openCustomErrorDialogue(title: String, message: String) {
        DialogBuilder.createFullCustomNotificationDialog(context, title, message)
    }

    override fun openSubscriptionScreen() {
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

    override fun openAccountNotActiveDialogue() {
        DialogBuilder.createNotificationDialog(context, Dialogs.ACCOUNT_NOT_ACTIVE)
    }

    override fun openAccountNotActiveBetaDialogue() {
        DialogBuilder.createNotificationDialog(context, Dialogs.ACCOUNT_NOT_ACTIVE_BETA)
    }

    override fun onLogin() {
        val action = LoginFragmentDirections.actionLoginFragmentToSyncFragment()
        findNavControllerSafely()?.navigate(action)
    }

    override fun onLoginWithBlankAccount() {
        if (viewModel.isAccountNewStyle()) {
            signUp.blankAccountID.set(viewModel.username.get())

            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavControllerSafely()?.navigate(action)
        } else {
            onLogin()
        }
    }

    override fun onLoginWithInactiveAccount() {
        if (viewModel.isAccountNewStyle()) {
            signUp.selectedPlan.set(Plan.getPlanByProductName(viewModel.getAccountType()))

            val action = LoginFragmentDirections.actionLoginFragmentToSignUpPeriodFragment()
            findNavControllerSafely()?.navigate(action)
        } else {
            onLogin()
        }
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
        findNavControllerSafely()?.navigate(action)
    }

    override fun onAccountCreationError() {
        DialogBuilder.createNotificationDialog(context, Dialogs.CREATE_ACCOUNT_ERROR)
    }

    fun Window.getSoftInputMode() : Int {
        return attributes.softInputMode
    }
}