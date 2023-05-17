package net.ivpn.core.v2.protocol.port

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.ui.theme.colorPrimary

@Composable
fun PortsScreen(navController: NavController?, viewModel: PortsViewModel) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn {
                items(viewModel.getPorts()) {
                    PortListItem(it, navController, viewModel)
                }
                item {
                    AddCustomPortAction(navController)
                }
            }
        }
    }
}

@Composable
fun PortListItem(port: Port, navController: NavController?, viewModel: PortsViewModel) {
    Row(
        modifier = Modifier
            .clickable {
                viewModel.setPort(port)
                navController?.popBackStack()
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text(port.toThumbnail())
        if (port == viewModel.getPort()) {
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.Check,
                tint = colorPrimary,
                contentDescription = "Selected"
            )
        }
    }
    Divider()
}

@Composable
fun AddCustomPortAction(navController: NavController?) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                val action = PortsFragmentDirections.actionPortFragmentToCustomPortFragment()
                navController?.navigate(action)
            }
        ) {
            Text("Add custom port".uppercase())
        }
    }
}
