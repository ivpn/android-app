package net.ivpn.client.ui.serverlist.all

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
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.dialog.DialogBuilder
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.serverlist.ServersListNavigator
import net.ivpn.client.ui.serverlist.ServersRecyclerViewAdapter
import net.ivpn.client.v2.serverlist.ServerFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ServerListFragment : Fragment(), ServersListNavigator {
    private lateinit var binding: FragmentServerListBinding

    @Inject
    lateinit var viewmodel: ServersListViewModel

//    lateinit var args: ServerListFragmentArgs

    //    private ServersListNavigator navigator;

    lateinit var serverType: ServerType


    override fun onAttach(context: Context) {
        super.onAttach(context)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
//        serverType = (parentFragment as ServerListFragment).getServerType()
        //        this.navigator = (ServersListNavigator) getActivity();

//        if (context instanceof ServersListActivity) {
//            serverType = ((ServersListActivity) context).getServerType();
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverType = (parentFragment as ServerFragment).getServerType()
//        args = ServerListFragmentArgs.fromBundle(arguments!!)
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
    }

    override fun onResume() {
        super.onResume()
        viewmodel.start(serverType)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SERVER_TYPE_STATE, serverType)
    }

    private fun init() {
//        println("serverType = ${ServerListFragmentArgs.fromBundle(arguments!!).serverType}")
        viewmodel.setServerType(serverType)
        binding.viewmodel = viewmodel
        val adapter = ServersRecyclerViewAdapter(this,
                viewmodel.isFastestServerAllowed)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent)
    }

    fun applyPendingAction() {
        LOGGER.info("applyPendingAction")
        viewmodel.applyPendingAction()
    }

    fun cancel() {
        LOGGER.info("cancel")
        viewmodel.cancel()
    }

    override fun onServerSelected(server: Server, forbiddenServer: Server?) {
        LOGGER.info("Server = $server forbidden server = $forbiddenServer")
        if (server.canBeUsedAsMultiHopWith(forbiddenServer)) {
            viewmodel.setCurrentServer(server)
            //TODO FINISH IT
            (parentFragment as ServerFragment).navigateBack()
        } else {
            DialogBuilder.createNotificationDialog(this.context, Dialogs.INCOMPATIBLE_SERVERS)
        }
    }

    override fun onServerLongClick(server: Server) {
        LOGGER.info("onServerLongClick server = $server")
        viewmodel.addFavouriteServer(server)
        (parentFragment as ServerFragment).notifyFavouritesChanged(true)
    }

    override fun onFastestServerSelected() {
        LOGGER.info("onFastestServerSelected")
        viewmodel.setSettingFastestServer()
        //TODO FINISH IT
        (parentFragment as ServerFragment).onFastestServerSelected()
    }

    override fun onFastestServerSettings() {
        LOGGER.info("onFastestServerSettings")
        //TODO START FASTEST SERVER SETTINGS
        (parentFragment as ServerFragment).onFastestServerSettings()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServerListFragment::class.java)
        private const val SERVER_TYPE_STATE = "SERVER_TYPE_STATE"
    }
}