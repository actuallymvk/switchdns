package com.switchdns.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.switchdns.data.DnsMode
import com.switchdns.data.DnsServer
import com.switchdns.ui.components.AddServerDialog
import com.switchdns.ui.components.ModeSelectorCard
import com.switchdns.ui.components.PermissionCard
import com.switchdns.ui.components.QuickSettingsTileHint
import com.switchdns.ui.components.ServerListCard
import com.switchdns.ui.components.TileSettingsCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    hasPermission: Boolean,
    packageName: String,
    currentMode: String,
    currentSpecifier: String,
    servers: List<DnsServer>,
    includeAutoInCycle: Boolean,
    onRefresh: () -> Unit,
    onModeChange: (String) -> Unit,
    onServerSelected: (DnsServer) -> Unit,
    onAddServer: (name: String, host: String) -> Unit,
    onDeleteServer: (DnsServer) -> Unit,
    onResetDefaults: () -> Unit,
    onToggleIncludeAuto: (Boolean) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Switch DNS",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Частный DNS • v1.1.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Логотип",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onRefresh()
                        scope.launch {
                            snackbarHostState.showSnackbar("Статус обновлен")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Banner / Status Card
            PermissionCard(
                hasPermission = hasPermission,
                packageName = packageName,
                onRefresh = {
                    onRefresh()
                    scope.launch {
                        if (hasPermission) {
                            snackbarHostState.showSnackbar("Разрешение активно!")
                        } else {
                            snackbarHostState.showSnackbar("Разрешение пока не получено")
                        }
                    }
                }
            )

            // DNS Mode Selector Card
            ModeSelectorCard(
                currentMode = currentMode,
                currentSpecifier = currentSpecifier,
                hasPermission = hasPermission,
                onModeSelected = { mode ->
                    if (!hasPermission) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Сначала предоставьте разрешение через ADB!")
                        }
                    } else {
                        onModeChange(mode)
                        scope.launch {
                            snackbarHostState.showSnackbar("Режим изменен: ${DnsMode.getDisplayName(mode)}")
                        }
                    }
                }
            )

            // Quick Settings Tile Configuration Card
            TileSettingsCard(
                includeAutoInCycle = includeAutoInCycle,
                onToggleIncludeAuto = onToggleIncludeAuto
            )

            // DNS Server List Card
            ServerListCard(
                servers = servers,
                currentMode = currentMode,
                currentSpecifier = currentSpecifier,
                onServerClick = { server ->
                    if (!hasPermission) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Сначала предоставьте разрешение через ADB!")
                        }
                    } else {
                        onServerSelected(server)
                        scope.launch {
                            snackbarHostState.showSnackbar("Выбран сервер: ${server.name}")
                        }
                    }
                },
                onDeleteServer = { server ->
                    onDeleteServer(server)
                    scope.launch {
                        snackbarHostState.showSnackbar("Сервер «${server.name}» удален")
                    }
                },
                onAddServerClick = { showAddDialog = true },
                onResetDefaults = {
                    onResetDefaults()
                    scope.launch {
                        snackbarHostState.showSnackbar("Список серверов сброшен по умолчанию")
                    }
                }
            )

            // Quick Settings Tile Hint
            QuickSettingsTileHint(
                includeAutoInCycle = includeAutoInCycle
            )
        }
    }

    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { showAddDialog = false },
            onAddServer = { name, host ->
                onAddServer(name, host)
                showAddDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Сервер «$name» добавлен")
                }
            }
        )
    }
}
