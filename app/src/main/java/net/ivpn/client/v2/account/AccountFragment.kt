package net.ivpn.client.v2.account

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
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.common.utils.ToastUtil
import net.ivpn.client.databinding.FragmentAccountBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.v2.login.LoginFragment
import net.ivpn.client.v2.viewmodel.AccountViewModel
import net.ivpn.client.v2.viewmodel.SignUpViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AccountFragment : Fragment(), AccountViewModel.AccountNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoginFragment::class.java)
    }

    lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var account: AccountViewModel

    @Inject
    lateinit var signUp: SignUpViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
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
        account.onResume()
        account.drawQR(resources.getColor(R.color.account_text), resources.getColor(R.color.account_background), binding.contentLayout.qr.width)
    }

    override fun onDestroy() {
        super.onDestroy()
        account.cancel()
    }

    private fun initViews() {
        binding.contentLayout.account = account
        account.navigator = this
        binding.contentLayout.logOut.setOnClickListener {
            DialogBuilder.createOptionDialog(context, Dialogs.LOGOUT) { _: DialogInterface?, _: Int -> account.logOut() }
        }

        binding.contentLayout.copyBtn.setOnClickListener {
            copyAccountId()
        }

        binding.contentLayout.addFunds.setOnClickListener {
            addFunds()
        }
    }

    override fun onLogOut() {
        NavHostFragment.findNavController(this).popBackStack()
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun copyAccountId() {
        account.username.get()?.let { userId ->
            val myClipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val myClip: ClipData = ClipData.newPlainText("User Id", userId)
            myClipboard.setPrimaryClip(myClip)

            ToastUtil.toast(R.string.account_clipboard)
        }
    }

    private fun addFunds() {
        if (account.isAccountNewStyle()) {
            signUp.selectedPlan.set(Plan.getPlanByProductName(account.accountType.get()))
            signUp.updateUserId()

            val action = AccountFragmentDirections.actionAccountFragmentToSignUpPeriodFragment()
            NavHostFragment.findNavController(this).navigate(action)
        } else {
            val url = "https://www.ivpn.net/account/login"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}