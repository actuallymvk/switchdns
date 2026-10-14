package com.switchdns.data

object DnsMode {
    const val OFF = "off"
    const val OPPORTUNISTIC = "opportunistic"
    const val HOSTNAME = "hostname"

    fun getDisplayName(mode: String): String {
        return when (mode) {
            OFF -> "Выключен"
            OPPORTUNISTIC -> "Автоматический"
            HOSTNAME -> "Частный DNS (свой сервер)"
            else -> "Неизвестно"
        }
    }

    fun getDescription(mode: String): String {
        return when (mode) {
            OFF -> "Шифрование DNS отключено"
            OPPORTUNISTIC -> "Используется DNS провайдера с шифрованием, если поддерживается"
            HOSTNAME -> "Все DNS-запросы шифруются через выбранный DoT сервер"
            else -> ""
        }
    }
}
