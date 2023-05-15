package net.ivpn.core.v2.protocol.port

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        ProtocolPort("TCP", 2005),
        ProtocolPort("TCP", 2006)
    )
    Surface {
        LazyColumn {
            items(portsList) {
                PortListItem(port = it)
            }
        }
    }
}

@Composable
fun PortListItem(port: ProtocolPort) {
    Row {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .clickable {

                }
        ) {
            Text(text = "${port.proto} ${port.port}")
        }
    }
    Divider()
}
