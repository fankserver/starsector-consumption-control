# Consumption Control

A vanilla-neutral [Starsector](https://fractalsoftworks.com/) 0.98a mod providing independent LunaLib multipliers for campaign fuel and supply consumption.

Installing the mod does not alter campaign balance: every multiplier defaults to `1.0`.

## Settings

- Hyperspace fuel consumption
- Monthly ship maintenance
- Combined repair and combat-readiness recovery supply cost
- Hull and armor repair speed
- Combat-readiness recovery speed

Every multiplier accepts values from `0.0` to `2.0`:

- `1.0`: vanilla
- `0.2`: 20% of vanilla
- `0.0`: disabled
- `2.0`: double vanilla

Only the hyperspace fuel multiplier is changed; normal-space travel and explicit campaign actions are untouched. Starsector exposes armor/hull repair and CR recovery supply cost through one combined ship stat, so those costs share one setting.

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
