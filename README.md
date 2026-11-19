# Damage Taken

Damage Taken is a RuneLite plugin that tracks incoming damage and attributes it to NPC sources when the client provides enough evidence.

![alt text][logo]
[logo]: https://github.com/Kewsco/DamageTaken/res/DamageTakenScreenshot.png "Damage taken overlay."

## Features

- Displays incoming damage totals in an overlay.
- Attributes projectile and melee hits to NPC names when possible.
- Preserves ambiguous hits as `Unknown` instead of guessing.
- Configures visible rows, overlay opacity, width, and header visibility.
- Clears recorded data from the overlay context menu or the configuration panel.

## Development

This project uses Java 11 and Gradle. Run the Gradle `run` task to launch the
development client, and the `test` task to run the test suite.

The plugin was built with the assistance of AI.

The official RuneLite Plugin Hub build uses the `runelite-plugin.properties` manifest and the `standard` build type.
