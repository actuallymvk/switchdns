package com.switchdns.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.switchdns.data.DnsMode

@Composable
fun ModeSelectorCard(
    currentMode: String,
    currentSpecifier: String,
    hasPermission: Boolean,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Режим работы DNS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1: Off
            ModeOptionRow(
                title = "Выключен",
                subtitle = "Частный DNS отключен. Запросы идут открытым текстом.",
                selected = currentMode == DnsMode.OFF,
                onClick = { onModeSelected(DnsMode.OFF) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Option 2: Opportunistic (Auto)
            ModeOptionRow(
                title = "Автоматический",
                subtitle = "DNS провайдера с поддержкой DoT шифрования, если доступно.",
                selected = currentMode == DnsMode.OPPORTUNISTIC,
                onClick = { onModeSelected(DnsMode.OPPORTUNISTIC) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Option 3: Hostname (Custom server)
            val hostnameSubtitle = if (currentSpecifier.isNotBlank()) {
                "Активный сервер: $currentSpecifier"
            } else {
                "Укажите сервер из списка ниже"
            }
            ModeOptionRow(
                title = "Частный DNS-сервер",
                subtitle = hostnameSubtitle,
                selected = currentMode == DnsMode.HOSTNAME,
                onClick = { onModeSelected(DnsMode.HOSTNAME) }
            )
        }
    }
}

@Composable
private fun ModeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
