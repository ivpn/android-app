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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import net.ivpn.core.R
import net.ivpn.core.rest.data.model.AntiTracker
import net.ivpn.core.ui.theme.colorPrimary

@Composable
fun AntiTrackerListScreen(navController: NavController?, viewModel: AntiTrackerListViewModel) {
    Surface {
        Column {
            LazyColumn {
                item {
                    AntiTrackerListSection(stringResource(R.string.anti_tracker_pre_defined))
                }
                items(viewModel.antiTrackerBasicList) {
                    AntiTrackerListItem(it, navController, viewModel)
                }
                item {
                    AntiTrackerListSection(stringResource(R.string.anti_tracker_individual))
                }
                items(viewModel.antiTrackerIndividualList) {
                    AntiTrackerListItem(it, navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun AntiTrackerListSection(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 18.dp, top = 32.dp, end = 18.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            color = colorPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
    Divider()
}

@Composable
fun AntiTrackerListItem(dns: AntiTracker, navController: NavController?, viewModel: AntiTrackerListViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                viewModel.setAntiTracker(dns)
                navController?.popBackStack()
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text(dns.toThumbnail())
        if (dns == viewModel.antiTracker) {
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
