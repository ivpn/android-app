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
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import net.ivpn.core.ui.theme.LocalColors

@Composable
fun CustomPortScreen(navController: NavController?, viewModel: CustomPortViewModel) {
    Surface {
        Column {
            PortInput(viewModel)
            SelectPortType(viewModel)
            SaveCustomPortAction()
        }
    }
}

@Composable
fun PortInput(viewModel: CustomPortViewModel) {
    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        TextFieldLabel("Port")
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
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
fun SelectPortType(viewModel: CustomPortViewModel) {
    if (viewModel.enableType) {
        val radioOptions = listOf("UDP", "TCP")
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(radioOptions[0])
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            TextFieldLabel("Type")
            radioOptions.forEach { text ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) }
                    )
                    Text(text)
                }
            }
        }
    }
}

@Composable
fun SaveCustomPortAction() {
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Button(onClick = {}) {
            Text(stringResource(R.string.settings_port_add_custom_port).uppercase())
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
