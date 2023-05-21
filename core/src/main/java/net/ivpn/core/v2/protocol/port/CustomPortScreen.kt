package net.ivpn.core.v2.protocol.port

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
import net.ivpn.core.R
import net.ivpn.core.ui.theme.LocalColors

@Composable
fun CustomPortScreen() {
    Surface {
        Column {
            PortInput()
            SelectPortType()
            SaveCustomPortAction()
        }
    }
}

@Composable
fun PortInput() {
    Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        TextFieldLabel("Port")
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("5500 - 19999, 30000 - 65000") },
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
fun SelectPortType() {
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
