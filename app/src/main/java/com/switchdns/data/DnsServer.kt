package com.switchdns.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class DnsServer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val isDefault: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("host", host)
            put("isDefault", isDefault)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): DnsServer {
            return DnsServer(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.getString("name"),
                host = json.getString("host"),
                isDefault = json.optBoolean("isDefault", false)
            )
        }

        fun listToJson(servers: List<DnsServer>): String {
            val array = JSONArray()
            for (server in servers) {
                array.put(server.toJsonObject())
            }
            return array.toString()
        }

        fun listFromJson(jsonString: String): List<DnsServer> {
            val list = mutableListOf<DnsServer>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(fromJsonObject(obj))
                }
            } catch (e: Exception) {
                // If parsing fails, return empty list
            }
            return list
        }

        fun getDefaultServers(): List<DnsServer> {
            return listOf(
                DnsServer(
                    id = "default-adguard",
                    name = "AdGuard",
                    host = "dns.adguard-dns.com",
                    isDefault = true
                ),
                DnsServer(
                    id = "default-cloudflare",
                    name = "Cloudflare",
                    host = "one.one.one.one",
                    isDefault = true
                ),
                DnsServer(
                    id = "default-google",
                    name = "Google",
                    host = "dns.google",
                    isDefault = true
                ),
                DnsServer(
                    id = "default-quad9",
                    name = "Quad9",
                    host = "dns.quad9.net",
                    isDefault = true
                )
            )
        }
    }
}
