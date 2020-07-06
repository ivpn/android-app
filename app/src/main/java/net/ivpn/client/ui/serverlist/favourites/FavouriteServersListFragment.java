package net.ivpn.client.ui.serverlist.favourites;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.prefs.OnFavouriteServersChangedListener;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.databinding.FragmentFavouriteServersListBinding;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.serverlist.ServersListNavigator;
import net.ivpn.client.ui.serverlist.ServersRecyclerViewAdapter;
import net.ivpn.client.v2.serverlist.ServerFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class FavouriteServersListFragment extends Fragment implements ServersListNavigator,
        OnFavouriteServersChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FavouriteServersListFragment.class);
    private static final String SERVER_TYPE_STATE = "SERVER_TYPE_STATE";

    private FragmentFavouriteServersListBinding binding;
    @Inject
    FavouriteServersListViewModel viewmodel;
    private ServersRecyclerViewAdapter adapter;
//    private ServersListNavigator navigator;

    private ServerType serverType;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            serverType = (ServerType) savedInstanceState.getSerializable(SERVER_TYPE_STATE);
            LOGGER.info("serverType = " + serverType);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
//        this.navigator = (ServersListNavigator) getActivity();

        serverType = ((ServerFragment) getParentFragment()).getServerType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_favourite_servers_list, container, false);
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewmodel.start(serverType);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SERVER_TYPE_STATE, serverType);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewmodel.removeFavouriteServerListener(this);
    }

    private void init(View view) {
        viewmodel.setServerType(serverType);
        viewmodel.addFavouriteServerListener(this);
        binding.setViewmodel(viewmodel);

        adapter = new ServersRecyclerViewAdapter(this, viewmodel.isFastestServerAllowed());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setEmptyView(view.findViewById(R.id.empty_view));
    }

    public void applyPendingAction() {
        viewmodel.applyPendingAction();
    }

    public void cancel() {
    }

    @Override
    public void onServerSelected(Server server, Server forbiddenServer) {
        LOGGER.info("onServerSelected server = " + server + " forbiddenServer = " + forbiddenServer);
        if (server.canBeUsedAsMultiHopWith(forbiddenServer)) {
            viewmodel.setCurrentServer(server);
            ((ServerFragment) getParentFragment()).navigateBack();
            //FINISH IT
//            navigator.onServerSelected(server, forbiddenServer);
        } else {
            DialogBuilder.createNotificationDialog(this.getContext(), Dialogs.INCOMPATIBLE_SERVERS);
        }
    }

    @Override
    public void onServerLongClick(Server server) {
        LOGGER.info("onServerLongClick server = " + server);
        viewmodel.removeFavouriteServer(server);
        adapter.removeServer(server);
        ((ServerFragment) getParentFragment()).notifyFavouritesChanged(false);
    }

    @Override
    public void onFastestServerSelected() {
        LOGGER.info("onFastestServerSelected");
        viewmodel.setSettingFastestServer();
        ((ServerFragment) getParentFragment()).onFastestServerSelected();
        //FINISH IT
//        navigator.onFastestServerSelected();
    }

    @Override
    public void onFastestServerSettings() {
        LOGGER.info("onFastestServerSettings");
        //TODO OPEN FASTEST SERVER
        ((ServerFragment) getParentFragment()).onFastestServerSettings();
//        navigator.onFastestServerSettings();
    }

    @Override
    public void notifyFavouriteServerAdded(Server server) {
        adapter.addServer(server);
    }

    @Override
    public void notifyFavouriteServerRemoved(Server server) {
        adapter.removeServer(server);
    }
}