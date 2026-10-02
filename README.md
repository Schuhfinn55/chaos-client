# ✸ Chaos Client

Die Fabric-Client-Mod des **Chaos Launchers** (ChaoscraftSMP). Minecraft **1.21.11**, Fabric Loader ≥ 0.16, Fabric API, Java 21. Eigenständige Implementierung im Chaos-Design (Rot/Schwarz/Dunkelgrau) – keine fremden Designs, Logos oder Texte.

Der Launcher legt die JAR beim Start jedes Fabric-/Quilt-Profils automatisch in `mods/` (wenn die MC-Version laut `fabric.mod.json` passt). Ingame: **RIGHT SHIFT** (frei belegbar) oder `/chaos` öffnet das CHAOS-Menü.

## Funktionen

| Bereich | Module |
|---|---|
| **Menü** | App-artiges Hauptmenü mit Suche, Kategorien (Allgemein, HUD, GUI, Spieler, World, PvP, Performance, Cosmetics, Social, Chat, Server, Screen, Audio, Misc, Chaos), Modul-Kacheln mit Toggle, Keybind und Config-Panel (Boolean, Slider, Farbe, Keybind, Dropdown, Text, Zahl) |
| **HUD-Editor** | `/chaos hud` – Drag & Drop, Snap-Raster, Ausrichtung an Kanten/Mitte/anderen Elementen, Scrollrad = Scale, Pfeiltasten, pro Element: Scale, Opacity, Rotation, Alignment, Padding, Textfarbe, Schatten, Hintergrund, Rahmen, Radius; alles persistent |
| **HUD** | FPS Counter, CPS Counter, Keystrokes (animiert), Coordinates (Richtung/Biom/Dimension/Speed), Server Info (FPS/Ping/TPS/IP/Spieler), Armor HUD, Potion Effects, Held Item, Health Bar, Movement, Clock, Scoreboard (Chaos-Design), Tablist (Ping-Zahlen, Freunde) |
| **PvP** | Crosshair-Editor mit Presets, Trefferfarbe & -animation, Damage Indicator / Damage Numbers (CRIT), Player Info (Ziel: Name, Abstand, Herzen, Rüstung, Item, Effekte), AutoGG |
| **Spieler / World** | ToggleSprint, ToggleSneak, Perspective (V), Free Look (ALT halten), Fullbright, Zoom (C, Smooth, Scrollrad), Block Overlay, Time Changer, Waypoints (persistent, HUD mit Entfernung/Pfeil, Kompassleiste) |
| **Performance** | Performance Center (`/chaos perf`): Modus LOW / BALANCED / HIGH / CUSTOM, Render-/Simulationsdistanz, Partikel, VSync, Wolken, Schatten, FPS-Limit – ausschließlich Render-Optionen, keine Gameplay-/Serverregel-relevanten Funktionen, Zustand immer sichtbar |
| **Cosmetics** | Capes aus dem Launcher ingame (eigenes Cape + Capes anderer Chaos-Spieler via Cosmetics-API/Cache), Cape-Bibliothek zum Wechseln mit Vorschau, Hüte/Wings/Back Items/Partikel (Kategorien vorbereitet), Emotes (erweiterbar, Direkttasten, `/chaos emote <id>`) |
| **Social / Server** | Freunde aus dem Launcher mit Online-Erkennung und Benachrichtigung, Party (server-seitig gesichert, vorbereitet), Server Quick Menu (N): Chaoscraft, gespeicherte Server |
| **Chat** | Zeitstempel, Mention-/Wort-Highlight mit Farbe & Sound, Filter (Werbung, Spam, Join/Quit, System, Private, eigene Wörter), eigene Chatfarbe, Transparenz/Größe/Breite |
| **Screen / Audio** | Fenstermodus (Fenster/Borderless/Vollbild), Screenshots (F12, Historie mit Vorschau, kein Auto-Upload ohne Zustimmung), Replay (nur mit Replay Mod – sonst klarer Hinweis statt Absturz), Music Player (eigene WAV/OGG aus dem Chaos-Musikordner), Lautstärkeregler Music/Effects/UI/Emotes/Cosmetics/Notifications |
| **System** | Notifications `[CHAOS] …` oben rechts, Keybind-Manager mit Konflikterkennung, Client-Profile (PvP, SMP, Chaoscraft, Recording …) inkl. Import/Export (`ChaosPvP.json`), Chaos Theme (Dark/Light, Akzent, Scale, Transparenz, Radius, Schatten, Animationen), ESC-Menü-Buttons (Vanilla bleibt erhalten) |

## Befehle

```
/chaos                 Menü öffnen
/chaos hud             HUD-Editor
/chaos cosmetics       Cosmetics
/chaos mods|settings   Module / GUI-Einstellungen
/chaos profile [Name]  Profile verwalten / wechseln
/chaos waypoint add|remove|list <Name>
/chaos emote <id>      Emote abspielen
/chaos server|music|perf|keybinds|social|screenshots|help
```

## Launcher ↔ Client

| Datei (im Spielprofil) | Richtung | Inhalt |
|---|---|---|
| `chaos-client/shared.json` | Launcher → Client | Launcher-/Client-Version, Menütaste, Musikordner, Profil, Account (Name/UUID – keine Tokens), Chaoscraft-Adresse, Server, Freunde |
| `chaos-cosmetics/config.json`, `cape.png`, `capes/<id>.png`, `players/`, `cache/` | Launcher → Client | Aktives Cape, Cape-Bibliothek, Capes anderer Spieler, API-Cache |
| `chaos-cosmetics/ingame-state.json` | Client → Launcher | Ingame gewähltes Cape (der Launcher übernimmt es beim Öffnen/Start) |
| `config/chaosclient/profiles/<Name>.json`, `waypoints.json` | Client | Module, HUD-Layout, Keybinds, Einstellungen pro Client-Profil; wird von Client-Updates nie angefasst |

## Build

```bash
./gradlew build                      # → build/libs/chaos-client-<version>.jar
cd ../onyx-launcher && bash sync-client.sh   # JAR in den Launcher kopieren
```

Struktur: `src/main/java/com/chaoscraft/client/` – `core` (Module, HudModule, ModuleManager, KeyManager), `modules/<kategorie>/`, `ui` (Screens, Widgets, Theme), `hud`, `config` (Profile, SharedData), `cosmetics`, `waypoints`, `server`, `social`, `emotes`, `music`, `screenshot`, `notifications`, `commands`, `mixin`.

## Lizenz

Chaos Client: MIT (siehe `LICENSE`). Verwendete Open-Source-Komponenten und deren Lizenzen: `src/main/resources/THIRD_PARTY_LICENSES.txt` (in der JAR enthalten). Minecraft ist eine Marke von Mojang Studios / Microsoft; dieses Projekt ist inoffiziell.
