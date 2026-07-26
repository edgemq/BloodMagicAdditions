# BMAddon

AE2 automation addon for Blood Magic on Minecraft 1.20.1 and NeoVitae on Minecraft/NeoForge 26.1.2.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-green.svg)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-NeoForge-orange.svg)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

## About

BMAddon connects Blood Magic-style crafting with Applied Energistics 2. The project page covers two release lines:

- Blood Magic builds for Minecraft 1.20.1.
- NeoVitae builds for Minecraft/NeoForge 26.1.2.

This branch targets the NeoVitae 26.1.2 line. It adds AE2-compatible automation for Ara Vitae and Tabula Vitae recipes, while using Essentia Vitae as the required fluid resource.

## Features

- Converts Forge Energy into Essentia Vitae.
- Adds an AE2-compatible Ara Vitae Assembler.
- Automates Ara Vitae recipes.
- Automates Tabula Vitae recipes.
- Adds custom encoded patterns for AE2 Pattern Encoding Terminal.
- Consumes Essentia Vitae from ME fluid storage.
- Returns crafted outputs to the ME network.
- Supports Ara Vitae tier cards II-V.
- Supports parallel processing and speed upgrades.
- Includes optional JEI integration.
- Includes English and Russian localization.
- Provides modpack-friendly common config values.

## BMAddon26.1.2-beta

This beta targets Minecraft 26.1.2, NeoForge 26.1.2, NeoVitae, and AE2 26.x.

Changes in this build:

- Updated item tooltips and client rendering calls for Minecraft/NeoVitae 26.1.2.
- Fixed Blood Generator GUI sync so stored energy and Essentia Vitae values update on the client.
- Fixed Ara Vitae Assembler pattern inventory updates so newly inserted encoded patterns are immediately visible to ME crafting.
- Adjusted Ara Vitae Assembler GUI positioning for the machine panel and player inventory.
- Reduced encoded Blood Pattern tooltip noise so encoded patterns are easier to read.

## Machines

### Blood Generator

Converts Forge Energy into Essentia Vitae and stores it as a fluid resource for automation.

### Ara Vitae Assembler

An AE2 crafting machine that runs supported NeoVitae recipes from encoded patterns.

Supported recipe families:

- Ara Vitae
- Tabula Vitae

## Patterns

The addon provides a custom pattern item for AE2. Encode it in the AE2 Pattern Encoding Terminal to store an Ara Vitae or Tabula Vitae recipe, then place the pattern in the assembler.

## Upgrades

| Upgrade | Description |
| --- | --- |
| Blood Speed Card | Faster crafting |
| Parallel Processing Card | Multiple active crafts |
| AE2 Speed Card | Native AE2 acceleration |
| Tier Cards II-V | Unlock higher Ara Vitae recipe tiers |

## Requirements

### NeoVitae 26.1.2 beta

- Minecraft 26.1.2
- NeoForge 26.1.2
- NeoVitae
- Applied Energistics 2
- Java 25

### Blood Magic 1.20.1 builds

- Minecraft 1.20.1
- Forge
- Blood Magic
- Applied Energistics 2

Optional:

- JEI

## Development

Build and compile with Gradle:

```powershell
.\gradlew.bat compileJava --no-configuration-cache
```

NeoVitae is resolved through CurseMaven in this port.

## License

This project is licensed under the GNU GPL v3. See [LICENSE](LICENSE) for details.
