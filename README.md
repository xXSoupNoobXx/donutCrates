# DonutCrates

DonutCrates ist ein leichtgewichtiges Spigot/Bukkit-Plugin zur Verwaltung von Crate-GUIs und Belohnungen. Einfache Konfiguration über YAML-Dateien und Editor-GUIs im Spiel.

## Features
- Konfigurierbare Crates mit Item-Rewards
- GUI-basierter Editor im Spiel
- Key-Management pro Spieler
- PlaceholderAPI-Unterstützung (`donutcrates`)
- Admin-Commands zum Erstellen / Verwalten von Crates

## Installation
1. Die JAR aus [Releases](https://github.com/xXSoupNoobXx/donutCrates/releases) herunterladen und in das `plugins`-Verzeichnis des Spigot-/Paper-Servers legen.
2. Server starten, damit die Standard-Konfigurationen erstellt werden.
3. Einstellungen in `config.yml`, `crates.yml` und `saves.yml` anpassen.
4. Server neu laden oder neu starten.

## Build (lokal)
- Mit Maven:
  - `mvn clean package`
- Die erzeugte JAR aus `target/` nach `plugins/` kopieren.

## Grundlegende Konfiguration
- Crates befinden sich unter `crates.yml`.
- Rewards haben Feldern wie:
  - `material`, `displayname`, `amount`, `slot`, `giveitem`, `lore`, `enchantments`, `command`
- Allgemeine Optionen in `config.yml` (z. B. `Sounds`, `Messages`).

## Wichtige Commands (permission: `donutcrates.admin`)
- ` /donutcrates create <name>` — Erstelle eine Crate an der aktuell betrachteten Blockposition (Player).
- ` /donutcrates delete <name>` — Lösche eine Crate.
- ` /donutcrates moveblock <name>` — Verschiebe Crate auf aktuell betrachteten Block.
- ` /donutcrates removeitem <crate> <itemKey>` — Entferne ein Reward aus einer Crate.
- ` /donutcrates addhanditem <crate>` — Füge das Item in der Hand als Reward zur Crate hinzu.
- ` /donutcrates keygive <player> <crate> <amount>` — Vergibt Keys an einen Spieler.
- ` /donutcrates removekey <player> <crate> <amount>` — Entfernt Keys von einem Spieler.
- ` /donutcrates keygiveall <crate> <amount>` — Vergibt Keys an alle Online-Spieler.
- ` /donutcrates editor` — Öffnet den GUI-Editor (Player).
- ` /donutcrates reload` — Lädt Konfiguration und Daten neu.
- ` /donutcrates list` — Listet vorhandene Crates auf.

(Genauere Usage \- Hinweise werden im Spiel als Hilfetexte angezeigt.)

## PlaceholderAPI
- Identifier: `donutcrates`
- Beispiel: `%donutcrates_key_<crate>%` gibt die Key-Anzahl für `<crate>` zurück.

## Daten & Persistenz
- Spielerdaten (Keys) und Cratepositionen werden in der Save-Datei gespeichert (`saves.yml`).
- Änderungen an Crates und Konfigurationen sollten mit `/donutcrates reload` oder durch Neustart übernommen werden.

## Fehlerbehebung
- Bei Problemen Server-Console-Log prüfen.
- Stellen sicher, dass die Material-/Sound-Namen mit der Server-Version kompatibel sind.
- Bei Trim-/Armor-Problemen (Minecraft-Versionabhängig) werden Warnungen ins Log geschrieben.
- Für weitere Hilfe/Bugreports ein ticket auf meinem Discord erstellen: `https://discord.gg/bzMcYKyr8g`
