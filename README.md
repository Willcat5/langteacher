# langteacher

Client-side Fabric mod that runs a timed loop of chat commands and messages. Designed for Chumm's event.

## What it does

Every `interval` seconds, while connected to a world and enabled, the mod:

1. Runs one command from your command list.
2. Sends the 3 configured chat messages, spaced half a second apart.

Commands are separated by commas and rotate one per cycle. For example
`/home, /spawn` sends `/home` on cycle 1, `/spawn` on cycle 2, `/home` again on cycle 3, and so on.
If you leave a blank command field, the command is skipped and only messages are sent.

## Controls

| Action | Key |
| --- | --- |
| Open settings | `H` (rebindable in Options > Controls > Key Binds, under its own "Langteacher" category) |

The settings screen can also be opened from the Mod Menu config button if Mod Menu is installed.

## Settings

Opened from the keybind or Mod Menu. Settings live in `config/langteacher.json` and are written when you click Save.

| Setting | Description |
| --- | --- |
| Enabled | Master switch. Loop runs while enabled and connected to a world. |
| Show HUD | Shows current cycle, countdown and next command in the top-left corner. |
| Interval (seconds) | Time between cycles. Minimum 1. |
| Command | Player command(s), comma-separated, e.g. `/home, /spawn`. One runs per cycle in rotation. |
| Message 1-3 | Chat messages sent after the command each cycle. Empty messages are skipped. |

## Notes

- This mod only sends ordinary player chat/command packets through the normal client path. It does not grant you permissions or bypass anything the server checks.
- Perfectly regular automated chat can look like a macro. Check the rules of any server you use it on.
- Messages or commands starting with `/` are sent through the command channel; anything else is sent as chat.

## Requirements

- Fabric Loader
- Fabric API
- Cloth Config

Optional: Mod Menu (adds a config button on the mods list).

## Build

```
./gradlew build
```

The jar is written to `build/libs/`.
