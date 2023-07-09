package net.ivpn.core.v2.antitracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.ivpn.core.rest.data.model.AntiTrackerDns
import net.ivpn.core.ui.theme.colorPrimary

@Composable
fun AntiTrackerListScreen(navController: NavController?, viewModel: AntiTrackerListViewModel) {
    Surface {
        Column {
            LazyColumn {
                items(viewModel.getAntiTrackerList()) {
                    AntiTrackerListItem(it, navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun AntiTrackerListItem(dns: AntiTrackerDns, navController: NavController?, viewModel: AntiTrackerListViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                viewModel.setAntiTrackerDns(dns)
                navController?.popBackStack()
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text(dns.toThumbnail())
        if (dns == viewModel.getAntiTrackerDns()) {
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
