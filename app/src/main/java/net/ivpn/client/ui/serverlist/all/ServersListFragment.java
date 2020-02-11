package net.ivpn.client.ui.serverlist.all;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.databinding.FragmentServerListBinding;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.serverlist.ServersListActivity;
import net.ivpn.client.ui.serverlist.ServersListNavigator;
import net.ivpn.client.ui.serverlist.ServersRecyclerViewAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ServersListFragment extends Fragment implements ServersListNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServersListFragment.class);
    private static final String SERVER_TYPE_STATE = "SERVER_TYPE_STATE";

    private FragmentServerListBinding binding;
    @Inject
    ServersListViewModel viewmodel;
    private ServersListNavigator navigator;

    private ServerType serverType;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            serverType = (ServerType) savedInstanceState.getSerializable(SERVER_TYPE_STATE);
            LOGGER.info("Created server list fragment, state = " + serverType);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        this.navigator = (ServersListNavigator) getActivity();

        if (context instanceof ServersListActivity) {
            serverType = ((ServersListActivity) context).getServerType();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_server_list, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewmodel.start(serverType);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SERVER_TYPE_STATE, serverType);
    }

    private void init() {
        viewmodel.setServerType(serverType);
        binding.setViewmodel(viewmodel);

        ServersRecyclerViewAdapter adapter = new ServersRecyclerViewAdapter(this,
                viewmodel.isFastestServerAllowed());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorAccent);
    }

    public void applyPendingAction() {
        LOGGER.info("applyPendingAction");
        viewmodel.applyPendingAction();
    }

    public void cancel() {
        LOGGER.info("cancel");
        viewmodel.cancel();
    }

    @Override
    public void onServerSelected(Server server, Server forbiddenServer) {
        LOGGER.info("Server = " + server + " forbidden server = " + forbiddenServer);
        if (server.canBeUsedAsMultiHopWith(forbiddenServer)) {
            viewmodel.setCurrentServer(server);
            navigator.onServerSelected(server, forbiddenServer);
        } else {
            DialogBuilder.createNotificationDialog(this.getContext(), Dialogs.INCOMPATIBLE_SERVERS);
        }
    }

    @Override
    public void onServerLongClick(Server server) {
        LOGGER.info("onServerLongClick server = " + server);
        viewmodel.addFavouriteServer(server);
        ((ServersListActivity) getActivity()).notifyFavouritesChanged(true);
    }

    @Override
    public void onFastestServerSelected() {
        LOGGER.info("onFastestServerSelected");
        viewmodel.setSettingFastestServer();
        navigator.onFastestServerSelected();
    }

    @Override
    public void onFastestServerSettings() {
        LOGGER.info("onFastestServerSettings");
        navigator.onFastestServerSettings();
    }
}