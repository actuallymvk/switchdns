package com.switchdns.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import com.switchdns.data.DnsMode
import com.switchdns.data.DnsRepository
import com.switchdns.data.DnsServer

class DnsSettingsManager(private val context: Context) {

    companion object {
        private const val TAG = "DnsSettingsManager"
        const val SETTING_PRIVATE_DNS_MODE = "private_dns_mode"
        const val SETTING_PRIVATE_DNS_SPECIFIER = "private_dns_specifier"
    }

    /**
     * Checks whether WRITE_SECURE_SETTINGS permission has been granted.
     */
    fun hasPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads current private_dns_mode from Settings.Global.
     * Returns "off", "opportunistic", or "hostname". Defaults to "opportunistic".
     */
    fun getMode(): String {
        return try {
            Settings.Global.getString(context.contentResolver, SETTING_PRIVATE_DNS_MODE) ?: DnsMode.OPPORTUNISTIC
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read private_dns_mode", e)
            DnsMode.OPPORTUNISTIC
        }
    }

    /**
     * Reads current private_dns_specifier from Settings.Global.
     */
    fun getSpecifier(): String {
        return try {
            Settings.Global.getString(context.contentResolver, SETTING_PRIVATE_DNS_SPECIFIER) ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read private_dns_specifier", e)
            ""
        }
    }

    /**
     * Updates private DNS mode and optionally specifier.
     * Returns true if successful, false otherwise.
     */
    fun setMode(mode: String, specifier: String? = null): Boolean {
        if (!hasPermission()) {
            Log.w(TAG, "Cannot write settings: WRITE_SECURE_SETTINGS permission is missing")
            return false
        }

        return try {
            val resolver = context.contentResolver
            when (mode) {
                DnsMode.OFF -> {
                    Settings.Global.putString(resolver, SETTING_PRIVATE_DNS_MODE, DnsMode.OFF)
                }
                DnsMode.OPPORTUNISTIC -> {
                    Settings.Global.putString(resolver, SETTING_PRIVATE_DNS_MODE, DnsMode.OPPORTUNISTIC)
                }
                DnsMode.HOSTNAME -> {
                    if (!specifier.isNullOrBlank()) {
                        val cleanHost = DnsRepository.normalizeHost(specifier)
                        Settings.Global.putString(resolver, SETTING_PRIVATE_DNS_SPECIFIER, cleanHost)
                    }
                    Settings.Global.putString(resolver, SETTING_PRIVATE_DNS_MODE, DnsMode.HOSTNAME)
                }
                else -> {
                    Settings.Global.putString(resolver, SETTING_PRIVATE_DNS_MODE, mode)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write private DNS settings", e)
            false
        }
    }

    /**
     * Cycles to the next DNS state:
     * If includeAuto == true:
     *   Off ("off") -> Auto ("opportunistic") -> Server 1 -> Server 2 -> ... -> Off
     * If includeAuto == false:
     *   Off ("off") -> Server 1 -> Server 2 -> ... -> Off
     */
    fun cycleNextMode(servers: List<DnsServer>, includeAuto: Boolean = true): String {
        val currentMode = getMode()
        val currentSpecifier = getSpecifier()

        return when (currentMode) {
            DnsMode.OFF -> {
                if (includeAuto) {
                    setMode(DnsMode.OPPORTUNISTIC)
                    DnsMode.OPPORTUNISTIC
                } else if (servers.isNotEmpty()) {
                    val firstServer = servers.first()
                    setMode(DnsMode.HOSTNAME, firstServer.host)
                    DnsMode.HOSTNAME
                } else {
                    setMode(DnsMode.OFF)
                    DnsMode.OFF
                }
            }
            DnsMode.OPPORTUNISTIC -> {
                if (servers.isNotEmpty()) {
                    val firstServer = servers.first()
                    setMode(DnsMode.HOSTNAME, firstServer.host)
                    DnsMode.HOSTNAME
                } else {
                    setMode(DnsMode.OFF)
                    DnsMode.OFF
                }
            }
            DnsMode.HOSTNAME -> {
                val currentIndex = servers.indexOfFirst { it.host.equals(currentSpecifier, ignoreCase = true) }
                if (currentIndex != -1 && currentIndex + 1 < servers.size) {
                    val nextServer = servers[currentIndex + 1]
                    setMode(DnsMode.HOSTNAME, nextServer.host)
                    DnsMode.HOSTNAME
                } else {
                    // Last server reached or current server unknown -> cycle back to OFF
                    setMode(DnsMode.OFF)
                    DnsMode.OFF
                }
            }
            else -> {
                setMode(DnsMode.OFF)
                DnsMode.OFF
            }
        }
    }
}
