# Veil Block (Minecraft Forge 1.12.2)

A Forge 1.12.2 prototype for configurable optical masking blocks.

## Current prototype

- Minecraft 1.12.2 / Forge 14.23.5.2860 / Java 8.
- Build system: ForgeGradle 3.0.196 with Gradle 5.6.4.
- Placeable Veil Block with a tile entity.
- Six independently configurable face modes stored in the tile entity.
- Sneak + left-click a face to cycle: Opaque -> Mask -> One-way window -> Glass.
- The client sends the requested face to the server; the server validates the player, distance, target block, and tile entity before changing it.
- Face settings persist in the world and synchronize to clients.
- Default mask depth: 16 blocks, stored per block and clamped to 1..128.

## Rendering status

The actual optical masking renderer is not implemented yet. In particular, the current block model is still an ordinary glass-textured cube: the face modes and mask depth are configuration data only. Per-viewer rendering, one-way visibility, depth limits, and masking of entities and particles still need implementation and in-game testing.

A correct implementation must make visibility decisions on each client, using that client's camera/view, rather than changing shared server block states. It must also avoid treating the translucent block model as if it could hide geometry behind it.

## Build

Use JDK 8 and Gradle 5.6.4, then run:

```bash
gradle build --no-daemon
```

The jar is created in `build/libs`. GitHub Actions also uploads it as the `veilblock-forge-1.12.2` workflow artifact after a successful build.
