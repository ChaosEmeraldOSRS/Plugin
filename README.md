# ChaosEmerald

A helper for the ChaosEmerald clan. It sends your loot and clan activity to a configurable url that defaults to ChaosEmerald's website.

> **Status:** in development and not on the Plugin Hub yet.

## Features

Every feature that sends data is **off by default**. Turn on only what you want in the plugin's settings.

### Send Loot
Sends a record whenever you receive loot:

- **Loot:** the source, your kill count, the items, the gear you're wearing and your inventory. Covers monster kills, chests, raids, clue caskets and minigame rewards. Pickpocketing is only sent for valuable items (100k+).
- **Raid completions:** sent when you finish a raid, with your completion count and party, so raid loot can be matched to a raid finished during an event.
- **Clue caskets:** sent when a clue's final step gives you a reward casket, with how many steps it took, so caskets earned during an event can be told apart from ones stored beforehand.
- **Barrows and Lunar Chest:** which Barrows brothers or Moons of Peril bosses were beaten before each chest.

Needs RuneLite's built-in **Loot Tracker** plugin to be turned on.

### Send Clan Coffer Broadcasts
Sends clan coffer deposits and withdrawals seen in clan chat.

### Sidebar
A ChaosEmerald panel in the RuneLite sidebar. Can be hidden or moved in the settings.

## What gets sent

Records go to the website URL in the plugin settings (the ChaosEmerald website by default). With each record the plugin sends your character name, the clan chat you're in and the world you're on. The website ignores anything not sent from the ChaosEmerald clan chat or from special worlds such as Leagues or Deadman.

Nothing is sent while the matching setting is off.

## Settings

| Setting | Default | What it does |
|---|---|---|
| Website URL | ChaosEmerald website | Where records are sent. Leave as default. |
| Send Loot | Off | Loot, raid completions, clue caskets, Barrows and Lunar Chest results |
| Send Clan Coffer Broadcasts | Off | Clan coffer deposits and withdrawals |
| Show sidebar | On | Shows the ChaosEmerald sidebar panel |
| Sidebar position | 5 | Lower numbers sit higher in the sidebar |

## Testing a build

You need Java 11 or newer.

1. Build the client with the plugin included:

   ```
   gradlew.bat shadowJar
   ```

   This creates `build/libs/chaosemerald-...-all.jar`.

2. To log in with a Jagex account, follow RuneLite's [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) guide once.

3. Start it:

   ```
   java -ea -jar build/libs/chaosemerald-...-all.jar
   ```

   `-ea` is required. RuneLite refuses to load a locally built plugin without it.

4. Turn the plugin on in RuneLite's plugin list and enable the settings you want to test.

## License

BSD 2-Clause. See [LICENSE](LICENSE).
