package net.ivpn.client.v2.serverlist.holders

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.pinger.OnPingFinishListener
import net.ivpn.client.common.pinger.PingProvider
import net.ivpn.client.common.pinger.PingResultFormatter
import net.ivpn.client.databinding.ServerItemBinding
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.serverlist.AdapterListener
import javax.inject.Inject

class ServerViewHolder(
        val binding: ServerItemBinding,
        val navigator: AdapterListener,
        val listener: HolderListener
) : RecyclerView.ViewHolder(binding.root), OnPingFinishListener {
    private val handler: Handler

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        handler = Handler(Looper.getMainLooper())
    }

    @Inject
    lateinit var pingProvider: PingProvider

    fun bind(server: Server, forbiddenServer: Server?) {
        binding.server = server
        binding.pingstatus = null
        binding.forbiddenServer = forbiddenServer
        binding.navigator = navigator
        binding.starLayout.setOnClickListener {
            server?.let {
                it.isFavourite = !it.isFavourite
                binding.star.setImageResource(if (it.isFavourite) R.drawable.ic_star_on else R.drawable.ic_star_off)
                navigator.changeFavouriteStateFor(it, it.isFavourite)
            }
        }
        binding.serverLayout.setOnClickListener {
            navigator.onServerSelected(server, forbiddenServer)
        }
        binding.executePendingBindings()
        pingProvider.ping(server, this)
    }

    override fun onPingFinish(status: PingResultFormatter) {
        handler.post {
            binding.pingstatus = status
            binding.executePendingBindings()
        }
    }
}