package com.switchdns.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.switchdns.MainActivity
import com.switchdns.R
import com.switchdns.data.DnsMode
import com.switchdns.data.DnsRepository

class DnsTileService : TileService() {

    private lateinit var dnsSettingsManager: DnsSettingsManager
    private lateinit var dnsRepository: DnsRepository

    override fun onCreate() {
        super.onCreate()
        dnsSettingsManager = DnsSettingsManager(this)
        dnsRepository = DnsRepository.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileUi()
    }

    override fun onClick() {
        super.onClick()
        if (!dnsSettingsManager.hasPermission()) {
            openMainActivity()
            return
        }

        val servers = dnsRepository.getServers()
        dnsSettingsManager.cycleNextMode(servers)
        updateTileUi()
    }

    private fun updateTileUi() {
        val tile = qsTile ?: return
        tile.icon = Icon.createWithResource(this, R.drawable.ic_dns)

        if (!dnsSettingsManager.hasPermission()) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_dns_name)
            tile.subtitle = "Требуется настройка"
            tile.updateTile()
            return
        }

        val mode = dnsSettingsManager.getMode()
        val specifier = dnsSettingsManager.getSpecifier()
        val servers = dnsRepository.getServers()

        when (mode) {
            DnsMode.OFF -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_dns_off)
                tile.subtitle = getString(R.string.tile_subtitle_off)
            }
            DnsMode.OPPORTUNISTIC -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_dns_auto)
                tile.subtitle = getString(R.string.tile_subtitle_auto)
            }
            DnsMode.HOSTNAME -> {
                tile.state = Tile.STATE_ACTIVE
                val matchedServer = servers.firstOrNull { it.host.equals(specifier, ignoreCase = true) }
                val serverName = matchedServer?.name ?: specifier.ifBlank { "Свой сервер" }
                tile.label = "DNS: $serverName"
                tile.subtitle = specifier.ifBlank { "Хост не задан" }
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_dns_name)
                tile.subtitle = getString(R.string.tile_subtitle_off)
            }
        }
        tile.updateTile()
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
