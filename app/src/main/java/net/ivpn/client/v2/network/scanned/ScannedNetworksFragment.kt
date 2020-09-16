package net.ivpn.client.v2.network.scanned

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.databinding.FragmentScannedNetworksBinding
import net.ivpn.client.v2.network.NetworkRecyclerViewAdapter
import net.ivpn.client.v2.network.NetworkViewModel
import javax.inject.Inject

class ScannedNetworksFragment : Fragment() {

    private lateinit var binding: FragmentScannedNetworksBinding
    private var adapter: NetworkRecyclerViewAdapter? = null

    @Inject
    lateinit var network: NetworkViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scanned_networks, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
        initViews()
    }

    private fun initViews() {
        adapter = NetworkRecyclerViewAdapter(activity)
        binding.viewmodel = network
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setEmptyView(binding.emptyView)

//        network.scanWifiNetworks()
    }
}