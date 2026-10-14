package com.switchdns

import android.content.ComponentName
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.switchdns.data.DnsMode
import com.switchdns.data.DnsRepository
import com.switchdns.data.DnsServer
import com.switchdns.service.DnsSettingsManager
import com.switchdns.service.DnsTileService
import com.switchdns.ui.screens.MainScreen
import com.switchdns.ui.theme.SwitchDNSTheme

class MainActivity : ComponentActivity() {

    private lateinit var dnsSettingsManager: DnsSettingsManager
    private lateinit var dnsRepository: DnsRepository

    private var hasPermission by mutableStateOf(false)
    private var currentMode by mutableStateOf(DnsMode.OPPORTUNISTIC)
    private var currentSpecifier by mutableStateOf("")
    private var servers by mutableStateOf<List<DnsServer>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dnsSettingsManager = DnsSettingsManager(this)
        dnsRepository = DnsRepository.getInstance(this)

        refreshState()

        setContent {
            SwitchDNSTheme {
                MainScreen(
                    hasPermission = hasPermission,
                    packageName = packageName,
                    currentMode = currentMode,
                    currentSpecifier = currentSpecifier,
                    servers = servers,
                    onRefresh = { refreshState() },
                    onModeChange = { mode ->
                        if (mode == DnsMode.HOSTNAME) {
                            val targetHost = if (currentSpecifier.isNotBlank()) {
                                currentSpecifier
                            } else {
                                servers.firstOrNull()?.host ?: ""
                            }
                            dnsSettingsManager.setMode(DnsMode.HOSTNAME, targetHost)
                        } else {
                            dnsSettingsManager.setMode(mode)
                        }
                        refreshState()
                        notifyTileUpdate()
                    },
                    onServerSelected = { server ->
                        dnsSettingsManager.setMode(DnsMode.HOSTNAME, server.host)
                        dnsRepository.setLastSelectedHost(server.host)
                        refreshState()
                        notifyTileUpdate()
                    },
                    onAddServer = { name, host ->
                        dnsRepository.addServer(name, host)
                        refreshState()
                        notifyTileUpdate()
                    },
                    onDeleteServer = { server ->
                        dnsRepository.removeServer(server.id)
                        refreshState()
                        notifyTileUpdate()
                    },
                    onResetDefaults = {
                        dnsRepository.resetToDefaults()
                        refreshState()
                        notifyTileUpdate()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatically re-checks permission and current DNS settings when returning to the app
        refreshState()
    }

    private fun refreshState() {
        hasPermission = dnsSettingsManager.hasPermission()
        currentMode = dnsSettingsManager.getMode()
        currentSpecifier = dnsSettingsManager.getSpecifier()
        servers = dnsRepository.getServers()
    }

    private fun notifyTileUpdate() {
        try {
            TileService.requestListeningState(
                this,
                ComponentName(this, DnsTileService::class.java)
            )
        } catch (_: Exception) {
            // Ignore if TileService is not bound or system rejects request
        }
    }
}
