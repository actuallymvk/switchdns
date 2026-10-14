package com.switchdns.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.switchdns.data.DnsRepository

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onAddServer: (name: String, host: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var hostError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить DNS-сервер") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    placeholder = { Text("Например: Mullvad или NextDNS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        hostError = null
                    },
                    label = { Text("Хост (DoT)") },
                    placeholder = { Text("dns.example.com") },
                    supportingText = {
                        Text(hostError ?: "Укажите имя хоста без https:// или tls://")
                    },
                    isError = hostError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalized = DnsRepository.normalizeHost(host)
                    if (normalized.isBlank()) {
                        hostError = "Хост не может быть пустым"
                        return@Button
                    }
                    if (normalized.contains(" ")) {
                        hostError = "Хост не должен содержать пробелов"
                        return@Button
                    }
                    val finalName = name.trim().ifBlank { normalized }
                    onAddServer(finalName, normalized)
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
