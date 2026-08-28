# Consumption Control

A vanilla-neutral [Starsector](https://fractalsoftworks.com/) 0.98a mod providing independent LunaLib multipliers for campaign fuel and supply consumption.

Installing the mod does not alter campaign balance: every multiplier defaults to `1.0`.

## Settings

- Hyperspace fuel consumption
- Monthly ship maintenance
- Field repair and combat-readiness recovery supply cost
- Dock repair cost (independently configurable; vanilla by default)
- Hull and armor repair speed
- Combat-readiness recovery speed

Every multiplier accepts values from `0.0` to `2.0`:

- `1.0`: vanilla
- `0.2`: 20% of vanilla
- `0.0`: disabled
- `2.0`: double vanilla

Only the hyperspace fuel multiplier is changed; normal-space travel and unrelated explicit campaign actions are untouched. Starsector exposes field recovery and dock repair pricing through one ship stat, so Consumption Control switches that stat contextually: the field multiplier applies while travelling and the dock multiplier applies while interacting with a market.

## Requirements

- Starsector 0.98a
- LunaLib 2.0.5 or compatible

## Installation

Copy the contents of `mod/` into a folder named `Consumption Control` below Starsector's `mods` directory, then enable **Consumption Control** in the launcher.

Configure it through LunaLib's in-game mod settings.

## Building without installing Java

Docker is the only build dependency:

```bash
./scripts/build-docker.sh
```

The script compiles against your local Starsector and LunaLib API jars in an ephemeral `eclipse-temurin:17-jdk` container. Override `STARSECTOR_DIR` if Starsector is installed elsewhere.

```bash
STARSECTOR_DIR='/path/to/Starsector' ./scripts/build-docker.sh
```

## Compatibility and safety

Modifiers use the unique key `consumption_control`, are reapplied live when LunaLib settings change, and are removed before saving. Disabling the master setting restores vanilla values without requiring a new campaign.

Do not use **No Maintenance Costs** at the same time if you want this mod's maintenance multiplier to be authoritative; that mod rewrites ship data directly.

## Releases

Push a strict Semantic Versioning tag to publish an install-ready GitHub Release:

```bash
git tag v1.2.3
git push origin v1.2.3
```

Prerelease tags such as `v1.2.3-beta.1` are supported and create prerelease GitHub Releases. The workflow derives the in-mod version from the tag, compiles the JAR inside Docker, and attaches `Consumption-Control-<version>.zip`. The ZIP contains a top-level `Consumption Control` directory ready to extract into `Starsector/mods`.

GitHub Actions compiles against signature-only API stubs because Starsector's proprietary API JAR cannot be redistributed. Local builds continue to compile against the actual installed API through `scripts/build-docker.sh`.
