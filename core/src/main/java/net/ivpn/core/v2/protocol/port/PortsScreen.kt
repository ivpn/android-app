package net.ivpn.core.v2.protocol.port

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
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
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.ui.theme.colorPrimary
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import net.ivpn.core.R
import net.ivpn.core.ui.theme.LocalColors

@Composable
fun PortsScreen(navController: NavController?, viewModel: PortsViewModel) {
    Surface {
        Column {
            LazyColumn {
                items(viewModel.getPorts()) {
                    PortListItem(it, navController, viewModel)
                }
                items(viewModel.getCustomPorts()) {
                    PortListItem(it, navController, viewModel, isCustom = true)
                }
                item {
                    AddCustomPortAction(navController)
                }
            }
        }
    }
}

@Composable
fun PortListItem(port: Port, navController: NavController?, viewModel: PortsViewModel, isCustom: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                viewModel.setPort(port)
                navController?.popBackStack()
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text(port.toThumbnail())
        if (isCustom) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.protocol_custom),
                color = LocalColors.current.secondaryLabel,
                fontSize = 11.sp
            )
        }
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
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Button(
            onClick = {
                val action = PortsFragmentDirections.actionPortFragmentToCustomPortFragment()
                navController?.navigate(action)
            }
        ) {
            Text(stringResource(R.string.protocol_add_custom_port).uppercase())
        }
    }
}
