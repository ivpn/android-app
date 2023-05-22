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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import net.ivpn.core.R
import net.ivpn.core.rest.data.model.Port
import net.ivpn.core.ui.theme.LocalColors

@Composable
fun CustomPortScreen(navController: NavController?, viewModel: CustomPortViewModel) {
    val portState = remember { mutableStateOf(TextFieldValue()) }
    val typeState = remember { mutableStateOf("UDP") }
    Surface {
        Column {
            PortInput(viewModel, portState)
            SelectPortType(viewModel, typeState)
            SaveCustomPortAction(navController, viewModel, portState, typeState)
        }
    }
}

@Composable
fun PortInput(viewModel: CustomPortViewModel, portState: MutableState<TextFieldValue>) {
    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        TextFieldLabel(stringResource(R.string.protocol_port))
        OutlinedTextField(
            value = portState.value,
            onValueChange = { portState.value = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(viewModel.portRangesText) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = LocalColors.current.textFieldBackground,
                unfocusedIndicatorColor = Color.Transparent,
                placeholderColor = LocalColors.current.textFieldPlaceholder,
                textColor = LocalColors.current.textFieldText
            )
        )
    }
}

@Composable
fun SelectPortType(viewModel: CustomPortViewModel, typeState: MutableState<String>) {
    if (viewModel.enableType) {
        val portTypes = listOf("UDP", "TCP")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            TextFieldLabel(stringResource(R.string.protocol_type))
            portTypes.forEach { text ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (text == typeState.value),
                        onClick = { typeState.value = text }
                    )
                    Text(text)
                }
            }
        }
    }
}

@Composable
fun SaveCustomPortAction(navController: NavController?, viewModel: CustomPortViewModel, portState: MutableState<TextFieldValue>, typeState: MutableState<String>) {
    val showErrorDialog = remember { mutableStateOf(false) }
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Button(onClick = {
            val portNumber = portState.value.text.toIntOrNull()
            if (viewModel.isValid(portNumber?: 0)) {
                val port = portNumber?.let { Port(typeState.value, it) }
                if (port != null) {
                    viewModel.addCustomPort(port)
                    navController?.popBackStack()
                }
            } else {
                showErrorDialog.value = true
            }
        }) {
            Text(stringResource(R.string.protocol_add_custom_port).uppercase())
        }
        if (showErrorDialog.value) {
            AlertDialog(
                onDismissRequest = { showErrorDialog.value = false },
                title = { Text(stringResource(R.string.dialogs_error)) },
                text = { Text(stringResource(R.string.protocol_valid_port_range) + " ${viewModel.portRangesText}") },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog.value = false }) {
                        Text(stringResource(R.string.dialogs_ok).uppercase())
                    }
                }
            )
        }
    }
}

@Composable
fun TextFieldLabel(text: String) {
    Text(
        text = text,
        color = LocalColors.current.textFieldLabel,
        modifier = Modifier.padding(vertical = 8.dp),
        fontSize = 14.sp
    )
}
