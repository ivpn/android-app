package net.ivpn.core.v2.protocol.port

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    Column(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        TextFieldLabel("Port")
        OutlinedTextField(
            value = "",
            onValueChange = { it },
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
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1] ) }
    Column(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        TextFieldLabel("Type")
        Column {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = {
                                onOptionSelected(text)
                            }
                        )
                        .padding(horizontal = 0.dp)
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) }
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1.merge(),
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SaveCustomPortAction() {
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = {}
        ) {
            Text(stringResource(R.string.settings_port_add_custom_port).uppercase())
        }
    }
}

@Composable
fun TextFieldLabel(text: String) {
    Text(
        text = text,
        color = LocalColors.current.textFieldLabel,
        modifier = Modifier.padding(vertical = 6.dp),
        fontSize = 14.sp
    )
}
