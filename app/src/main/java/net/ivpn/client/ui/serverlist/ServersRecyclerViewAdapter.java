//package net.ivpn.client.ui.serverlist;
//
//import android.os.Handler;
//import android.os.Looper;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import net.ivpn.client.IVPNApplication;
//import net.ivpn.client.common.pinger.OnPingFinishListener;
//import net.ivpn.client.common.pinger.PingProvider;
//import net.ivpn.client.common.pinger.PingResultFormatter;
//import net.ivpn.client.databinding.FastestServerItemBinding;
//import net.ivpn.client.databinding.ServerItemBinding;
//import net.ivpn.client.rest.data.model.Server;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import javax.inject.Inject;
//
//public class ServersRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private static final int FASTEST_SERVER_ITEM = 0;
//    private static final int SERVER_ITEM = 1;
//    private static final int SEARCH_ITEM = 2;
//    private static final int RANDOM_ITEM = 3;
//
//    private List<Server> servers = Collections.emptyList();
//    private FastestServerItemBinding binding;
//    private Server forbiddenServer;
//    private ServersListNavigator navigator;
//    private boolean isFastestServerAllowed;
//
//    public ServersRecyclerViewAdapter(ServersListNavigator navigator, boolean allowFastestServer) {
//        this.navigator = navigator;
//        this.isFastestServerAllowed = allowFastestServer;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        if (isFastestServerAllowed && position == 0) {
//            return FASTEST_SERVER_ITEM;
//        }
//        return SERVER_ITEM;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//        if (viewType == SERVER_ITEM) {
//            ServerItemBinding binding = ServerItemBinding.inflate(layoutInflater, parent, false);
//            return new ServerViewHolder(binding);
//        } else {
//            FastestServerItemBinding binding = FastestServerItemBinding.inflate(layoutInflater, parent, false);
//            return new FastestServerViewHolder(binding);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof ServerViewHolder) {
//            ((ServerViewHolder) holder).bind(getServerFor(position));
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return isFastestServerAllowed ? servers.size() + 1 : servers.size();
//    }
//
//    public void replaceData(List<Server> servers) {
//        setServers(new ArrayList<>(servers));
//    }
//
//    public void removeServer(Server server) {
//        if (!servers.contains(server)) {
//            return;
//        }
//
//        int position = getPositionFor(server);
//        servers.remove(server);
//        if (binding != null) {
//            binding.setIsServerListEmpty(servers.isEmpty());
//            binding.executePendingBindings();
//        }
//        notifyItemRemoved(position);
//    }
//
//    public void addServer(Server server) {
//        if (servers.contains(server)) {
//            return;
//        }
//
//        servers.add(server);
//        Collections.sort(servers, Server.comparator);
//
//        int position = getPositionFor(server);
//        if (binding != null) {
//            binding.setIsServerListEmpty(servers.isEmpty());
//            binding.executePendingBindings();
//        }
//        notifyItemInserted(position);
//    }
//
//    private void setServers(List<Server> servers) {
//        Collections.sort(servers, Server.comparator);
//        this.servers = servers;
//        if (binding != null) {
//            binding.setIsServerListEmpty(servers.isEmpty());
//            binding.executePendingBindings();
//        }
//        notifyDataSetChanged();
//    }
//
//    public void setForbiddenServer(Server server) {
//        forbiddenServer = server;
//    }
//
//    private int getPositionFor(Server server) {
//        return isFastestServerAllowed ? servers.indexOf(server) + 1 : servers.indexOf(server);
//    }
//
//    private Server getServerFor(int position) {
//        return isFastestServerAllowed ? servers.get(position - 1) : servers.get(position);
//    }
//
//    public class ServerViewHolder extends RecyclerView.ViewHolder implements OnPingFinishListener {
//
//        private ServerItemBinding binding;
//        private Handler handler;
//        @Inject PingProvider pingProvider;
//
//        ServerViewHolder(ServerItemBinding binding) {
//            super(binding.getRoot());
//            IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
//            this.binding = binding;
//            handler = new Handler(Looper.getMainLooper());
//        }
//
//        public void bind(Server server) {
//            binding.setServer(server);
//            binding.setPingstatus(null);
//            binding.setForbiddenServer(forbiddenServer);
//            binding.setNavigator(navigator);
//            binding.executePendingBindings();
//            pingProvider.ping(server, this);
//        }
//
//        @Override
//        public void onPingFinish(final PingResultFormatter status) {
//            handler.post(() -> {
//                binding.setPingstatus(status);
//                binding.executePendingBindings();
//            });
//        }
//    }
//
//    class FastestServerViewHolder extends RecyclerView.ViewHolder {
//
//        FastestServerViewHolder(FastestServerItemBinding binding) {
//            super(binding.getRoot());
//            ServersRecyclerViewAdapter.this.binding = binding;
//            binding.setNavigator(navigator);
//            binding.setIsServerListEmpty(servers.isEmpty());
//            binding.executePendingBindings();
//        }
//    }
//}