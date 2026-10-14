package com.switchdns.data

import android.content.Context
import android.content.SharedPreferences

class DnsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dns_settings_prefs"
        private const val KEY_SERVERS = "saved_dns_servers"
        private const val KEY_LAST_SELECTED_SERVER_HOST = "last_selected_server_host"
        private const val KEY_INCLUDE_AUTO_IN_CYCLE = "include_auto_in_cycle"

        @Volatile
        private var instance: DnsRepository? = null

        fun getInstance(context: Context): DnsRepository {
            return instance ?: synchronized(this) {
                instance ?: DnsRepository(context.applicationContext).also { instance = it }
            }
        }

        fun normalizeHost(rawHost: String): String {
            var host = rawHost.trim()
            val prefixes = listOf("https://", "http://", "tls://", "dns://")
            for (prefix in prefixes) {
                if (host.startsWith(prefix, ignoreCase = true)) {
                    host = host.substring(prefix.length)
                }
            }
            // Strip any trailing slashes or paths
            val slashIndex = host.indexOf('/')
            if (slashIndex != -1) {
                host = host.substring(0, slashIndex)
            }
            // Strip any port if entered
            val colonIndex = host.indexOf(':')
            if (colonIndex != -1) {
                host = host.substring(0, colonIndex)
            }
            return host.trim()
        }
    }

    fun getServers(): List<DnsServer> {
        val json = prefs.getString(KEY_SERVERS, null)
        if (json.isNullOrBlank()) {
            val defaults = DnsServer.getDefaultServers()
            saveServers(defaults)
            return defaults
        }
        val servers = DnsServer.listFromJson(json)
        return if (servers.isEmpty()) {
            val defaults = DnsServer.getDefaultServers()
            saveServers(defaults)
            defaults
        } else {
            servers
        }
    }

    fun saveServers(servers: List<DnsServer>) {
        val json = DnsServer.listToJson(servers)
        prefs.edit().putString(KEY_SERVERS, json).apply()
    }

    fun addServer(name: String, rawHost: String): DnsServer {
        val cleanHost = normalizeHost(rawHost)
        val cleanName = name.trim().ifBlank { cleanHost }
        val currentServers = getServers().toMutableList()

        // Check if server with this host already exists
        val existingIndex = currentServers.indexOfFirst { it.host.equals(cleanHost, ignoreCase = true) }
        val newServer = DnsServer(name = cleanName, host = cleanHost, isDefault = false)

        if (existingIndex >= 0) {
            currentServers[existingIndex] = newServer
        } else {
            currentServers.add(newServer)
        }
        saveServers(currentServers)
        return newServer
    }

    fun removeServer(serverId: String): Boolean {
        val currentServers = getServers().toMutableList()
        val removed = currentServers.removeAll { it.id == serverId }
        if (removed) {
            saveServers(currentServers)
        }
        return removed
    }

    fun resetToDefaults(): List<DnsServer> {
        val defaults = DnsServer.getDefaultServers()
        saveServers(defaults)
        return defaults
    }

    fun getLastSelectedHost(): String? {
        return prefs.getString(KEY_LAST_SELECTED_SERVER_HOST, null)
    }

    fun setLastSelectedHost(host: String) {
        prefs.edit().putString(KEY_LAST_SELECTED_SERVER_HOST, host).apply()
    }

    fun isIncludeAutoInCycle(): Boolean {
        return prefs.getBoolean(KEY_INCLUDE_AUTO_IN_CYCLE, true)
    }

    fun setIncludeAutoInCycle(include: Boolean) {
        prefs.edit().putBoolean(KEY_INCLUDE_AUTO_IN_CYCLE, include).apply()
    }
}
