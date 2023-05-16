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
import androidx.navigation.NavController
import net.ivpn.core.rest.data.model.Port

@Composable
fun PortsScreen(navController: NavController?, viewModel: PortsViewModel) {
    Surface {
        LazyColumn {
            items(viewModel.getPorts()) {
                PortListItem(port = it, navController = navController)
            }
        }
    }
}

@Composable
fun PortListItem(port: Port, navController: NavController?) {
    Row(
        modifier = Modifier
            .clickable {
                navController?.popBackStack()
            }
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {
            Text(text = "${port.protocol} ${port.portNumber}")
        }
    }
    Divider()
}
