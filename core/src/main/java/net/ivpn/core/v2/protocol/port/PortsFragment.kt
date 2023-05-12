package net.ivpn.core.v2.protocol.port

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.fragment.app.Fragment
import androidx.compose.runtime.Composable
import androidx.compose.material.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.R
import net.ivpn.core.databinding.FragmentPortsBinding

class PortsFragment : Fragment() {

    lateinit var binding: FragmentPortsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ports, container, false)
        return binding.root.apply {
            findViewById<ComposeView>(R.id.view_ports).setContent {
                PortsScreen()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

}

data class ProtocolPort(
    val proto: String,
    val port: Int
)

@Composable
fun PortsScreen() {

    val portsList = listOf(
        ProtocolPort("UDP", 2001),
        ProtocolPort("UDP", 2002),
        ProtocolPort("UDP", 2003),
        ProtocolPort("UDP", 2004),
        ProtocolPort("TCP" , 2005),
        ProtocolPort("TCP", 2006)
    )

    LazyColumn(
        contentPadding = PaddingValues(8.dp)
    ) {
        items(portsList) { item ->
            Text(text = "${item.proto} ${item.port}")
            Divider()
        }
    }

}
