package net.ivpn.client.v2.account

import android.content.DialogInterface
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
import net.ivpn.client.databinding.FragmentAccountBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.v2.login.LoginFragment
import net.ivpn.client.v2.viewmodel.AccountViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AccountFragment: Fragment(), AccountViewModel.AccountNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoginFragment::class.java)
    }

    lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var account: AccountViewModel

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
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onLogOut() {
        NavHostFragment.findNavController(this).popBackStack()
    }
}