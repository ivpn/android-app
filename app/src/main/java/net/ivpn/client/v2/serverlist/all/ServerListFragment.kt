package net.ivpn.client.v2.serverlist.all

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.databinding.FragmentServerListBinding
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.v2.serverlist.ServerListTabFragment
import net.ivpn.client.v2.viewmodel.ServerListViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ServerListFragment : Fragment(), ServerListViewModel.ServerListNavigator {
    private lateinit var binding: FragmentServerListBinding

    @Inject
    lateinit var viewmodel: ServerListViewModel

    lateinit var serverType: ServerType
    lateinit var adapter : AllServersRecyclerViewAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverType = (parentFragment as ServerListTabFragment).getServerType()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_server_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        viewmodel.start(serverType)
    }

    override fun onResume() {
        super.onResume()
        viewmodel.navigators.add(this)
//        viewmodel.start(serverType)
    }

    override fun onPause() {
        super.onPause()
        viewmodel.navigators.remove(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewmodel.favouriteListeners.remove(adapter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SERVER_TYPE_STATE, serverType)
    }

    private fun init() {
        viewmodel.setServerType(serverType)
        binding.viewmodel = viewmodel
        adapter = AllServersRecyclerViewAdapter(viewmodel.adapterListener,
                viewmodel.isFastestServerAllowed())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent)
        viewmodel.favouriteListeners.add(adapter)
    }

    fun cancel() {
        LOGGER.info("cancel")
        viewmodel.cancel()
    }
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServerListFragment::class.java)
        private const val SERVER_TYPE_STATE = "SERVER_TYPE_STATE"
    }

    override fun navigateBack() {
        (parentFragment as ServerListTabFragment).navigateBack()
    }

    override fun showDialog(dialogs: Dialogs) {
        DialogBuilder.createNotificationDialog(this.context, Dialogs.INCOMPATIBLE_SERVERS)
    }

    override fun openFastestSetting() {
        (parentFragment as ServerListTabFragment).openFastestSetting()
    }
}