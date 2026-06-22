# BlockUI (Fabric Fork)

Personal Fabric-only fork of [BlockUI](https://github.com/ldtteam/BlockUI), the XML-based UI library mod for Minecraft. This fork targets **Fabric Loader** on **Minecraft 1.21.1** and is not compatible with NeoForge.

The upstream project lives at [ldtteam/BlockUI](https://github.com/ldtteam/BlockUI). This fork is maintained at [RachaelsDen/BlockUI](https://github.com/RachaelsDen/BlockUI) on the `version/fabric` branch.

## Build

Requires **Java 21**. The build environment uses btrfs, which can race on Gradle's binary test result files. Always use the full override flags:

```bash
export JAVA_HOME="$HOME/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean build --no-daemon --no-build-cache --no-configuration-cache -Dorg.gradle.workers.max=1
```

Shorter forms like `./gradlew build` are not equivalent in this environment.

## Branch structure

| Branch | Purpose |
|---|---|
| `version/main` | Tracks upstream `ldtteam/BlockUI version/main` (NeoForge). Do not push to this. |
| `version/fabric` | Fabric port. Forked from `version/main` at commit `78dae57f`. All Fabric-specific work lives here. |

The `fabric-base` tag marks the exact fork point (`78dae57f044e47f0e9b31390dda86f861963d402`).

## Run dev client

```bash
./gradlew runClient
```

This launches Minecraft 1.21.1 with BlockUI and Sodium 0.6.13 in the dev environment.

## Port details

See [PORT-NOTES.md](PORT-NOTES.md) for the full port documentation, including:

- Event and callback mapping (NeoForge to Fabric)
- Networking API translation
- Config system (Cloth Config backend)
- Mixin and Access Widener inventories
- Known deviations from upstream behavior
- Cherry-pick guide for applying upstream fixes
