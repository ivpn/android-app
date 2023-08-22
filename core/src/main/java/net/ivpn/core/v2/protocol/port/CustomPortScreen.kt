package net.ivpn.core.v2.protocol.port

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 IVPN Limited.

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
            AddCustomPortAction(navController, viewModel, portState, typeState)
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
            singleLine = true,
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
fun AddCustomPortAction(navController: NavController?, viewModel: CustomPortViewModel, portState: MutableState<TextFieldValue>, typeState: MutableState<String>) {
    val errorMessage = remember { mutableStateOf("") }
    val errorIsValid = stringResource(R.string.protocol_valid_port_range) + " ${viewModel.portRangesText}"
    val errorIsDuplicate = stringResource(R.string.protocol_port_already_exists)
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Button(onClick = {
            val portNumber = portState.value.text.toIntOrNull()
            val port = portNumber?.let { Port(typeState.value, it) }
            if (port == null) {
                errorMessage.value = errorIsValid
            } else if (!viewModel.isValid(port)) {
                errorMessage.value = errorIsValid
            } else if (viewModel.isDuplicate(port)) {
                errorMessage.value = errorIsDuplicate
            } else {
                viewModel.addCustomPort(port)
                navController?.popBackStack()
            }
        }) {
            Text(stringResource(R.string.protocol_add_custom_port).uppercase())
        }
        if (errorMessage.value.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { errorMessage.value = "" },
                title = { Text(stringResource(R.string.dialogs_error), fontSize = 20.sp) },
                text = { Text(errorMessage.value, fontSize = 16.sp) },
                confirmButton = {
                    TextButton(onClick = { errorMessage.value = "" }) {
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
