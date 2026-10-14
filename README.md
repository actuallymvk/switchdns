# Switch DNS (Private DNS Quick Toggle)

Полноценное Android-приложение на Kotlin + Jetpack Compose (Material 3) для быстрого переключения системной настройки **Частный DNS (Private DNS / DNS-over-TLS)** через шторку быстрых настроек (Quick Settings Tile) и экран приложения.

---

## 🚀 Возможности

1. **Плитка в шторке (Quick Settings Tile):**
   - Циклическое переключение режимов в один клик:
     `Выкл (off)` ➔ `Авто (opportunistic)` ➔ `Сервер 1` ➔ `Сервер 2` ➔ ... ➔ `Выкл`
   - Индикация состояния:
     - **Выкл:** неактивна, статус «Не используется»
     - **Авто:** активна, статус «Системный»
     - **Свой сервер:** активна, название и адрес хоста
   - Полная поддержка Android 14+ (API 34+) через `PendingIntent` в `startActivityAndCollapse`
2. **Экран настроек (Jetpack Compose Material 3):**
   - Переключатель режимов: Выкл / Авто / Выбранный сервер
   - Предустановленные серверы:
     - **AdGuard:** `dns.adguard-dns.com`
     - **Cloudflare:** `one.one.one.one`
     - **Google:** `dns.google`
     - **Quad9:** `dns.quad9.net`
   - Добавление собственных серверов (с автоматической очисткой префиксов `https://`, `tls://` и пробелов)
   - Удаление и сброс серверов к исходным
   - Автоматическое обновление статуса разрешения в `onResume`
   - Баннер с командой ADB и кнопкой копирования в буфер обмена

---

## 🔑 Выдача разрешения через ADB

Так как Android защищает настройку `Settings.Global.private_dns_mode`, приложению требуется системное разрешение `WRITE_SECURE_SETTINGS`. Root-права **не требуются**.

1. Включите на телефоне **Отладку по USB** (Настройки ➔ Для разработчиков ➔ Отладка по USB).
2. Подключите телефон к компьютеру.
3. Выполните команду в терминале (команда также копируется одной кнопкой прямо из приложения):

```bash
adb shell pm grant com.switchdns android.permission.WRITE_SECURE_SETTINGS
```

После выполнения команды вернитесь в приложение — статус обновится автоматически!

---

## 🛠 Автоматическая сборка APK (GitHub Actions)

В проекте настроен workflow `.github/workflows/build.yml`, который автоматически собирает `.apk` при каждом push в ветку `main` или `master`, а также доступен для ручного запуска (`workflow_dispatch`).

### Как запустить и получить APK:
1. Запушьте проект на GitHub:
   ```bash
   git remote add origin https://github.com/<ваш_логин>/<имя_репозитория>.git
   git branch -M main
   git push -u origin main
   ```
2. Перейдите во вкладку **Actions** в репозитории на GitHub.
3. После завершения шага **Build Debug APK** скачайте готовый файл из секции **Artifacts** (`dns-toggle-debug-apk`).

---

## 📦 Стек технологий

- **Язык:** Kotlin 2.0.21, JDK 17
- **Сборка:** Gradle 8.9 (Kotlin DSL `kts`), AGP 8.7.3
- **UI:** Jetpack Compose + Material 3, Material Icons Extended
- **SDK:** Min SDK 29 (Android 10), Target/Compile SDK 35 (Android 15)
- **Хранение данных:** SharedPreferences + JSON
