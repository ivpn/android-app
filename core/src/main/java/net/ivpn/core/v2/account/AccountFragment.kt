package net.ivpn.core.v2.account

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.
 
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

import android.content.*
import android.net.Uri
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
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.billing.addfunds.Plan
import net.ivpn.core.common.extension.findNavControllerSafely
import net.ivpn.core.common.extension.navigate
import net.ivpn.core.common.utils.ToastUtil
import net.ivpn.core.databinding.FragmentAccountBinding
import net.ivpn.core.rest.data.session.SessionErrorResponse
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.connect.ConnectFragmentDirections
import net.ivpn.core.v2.signup.SignUpController
import net.ivpn.core.v2.viewmodel.AccountViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AccountFragment : Fragment(), AccountViewModel.AccountNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AccountFragment::class.java)
    }

    lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var account: AccountViewModel

    var signUp: SignUpController = IVPNApplication.signUpController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
        updateSessionStatus()
    }

    override fun onResume() {
        super.onResume()
        account.onResume()
        if (!account.authenticated.get()) {
            openHomeScreen()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        account.cancel()
    }

    private fun initViews() {
        binding.contentLayout.account = account
        account.navigator = this
        binding.contentLayout.logOut.setOnClickListener {
            openLogOutDialogue()
        }

        binding.contentLayout.copyBtn.setOnClickListener {
            copyAccountId()
        }

        binding.contentLayout.addFunds.setOnClickListener {
            addFunds()
        }
        binding.contentLayout.qr.post {
            account.drawQR(
                resources.getColor(R.color.account_qr_foreground),
                resources.getColor(R.color.account_qr_background),
                binding.contentLayout.qr.width
            )
        }
    }

    override fun onLogOut() {
        signUp.reset()

        try {
            NavHostFragment.findNavController(this).popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun updateSessionStatus() {
        account.updateSessionStatus()
    }

    private fun copyAccountId() {
        account.username.get()?.let { userId ->
            val myClipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val myClip: ClipData = ClipData.newPlainText("User Id", userId)
            myClipboard.setPrimaryClip(myClip)

            ToastUtil.toast(R.string.account_clipboard)
        }
    }

    private fun addFunds() {
        if (account.isAccountNewStyle()) {
            signUp.signUpWithInactiveAccount(
                findNavControllerSafely(),
                Plan.getPlanByProductName(account.accountType.get()), account.isAccountNewStyle()
            )
        } else {
            val url = "https://www.ivpn.net/account/login"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun openLogOutDialogue() {
        val action = AccountFragmentDirections.actionAccountFragmentToLogOutFragment()
        navigate(action)
    }

    private fun openLoginScreen() {
        val action = AccountFragmentDirections.actionAccountFragmentToLoginFragment(true)
        navigate(action)
    }

    private fun openHomeScreen() {
        val action = AccountFragmentDirections.actionAccountFragmentToConnectFragment()
        navigate(action)
    }

    override fun onLogOutFailed() {
        DialogBuilder.createOptionDialog(requireContext(), Dialogs.FORCE_LOGOUT) {
            account.forceLogout()
        }
    }

    override fun onDeviceLoggedOut() {
        openLoginScreen()
    }
}