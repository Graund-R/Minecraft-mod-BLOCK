# Veil Block (Minecraft Forge 1.12.2)

A Forge 1.12.2 prototype for configurable optical masking blocks.

## Current prototype

- Minecraft 1.12.2 / Forge 14.23.5.2860 / Java 8.
- Build system: ForgeGradle 3.0.196 with Gradle 5.6.4.
- Placeable Veil Block with a tile entity.
- Six independently configurable face modes stored in the tile entity.
- Sneak + left-click a face to cycle: Opaque -> Mask -> One-way window -> Glass.
- The server validates the player, distance, target block, and selected face before changing it.
- Face settings persist in the world and synchronize to clients.
- Default mask depth: 16 blocks, stored per block and clamped to 1..128.

## Rendering prototype

A client-side depth-only pre-pass has been added and is injected into
`EntityRenderer.renderWorldPass` immediately before the SOLID terrain layer.
It writes Veil face planes to the depth buffer without writing color, so
ordinary terrain, entities, and particles rendered later can be depth-occluded
for that client only. It does not change shared server block states.

Current face handling in this prototype:

- `OPAQUE` and `MASK`: write an occluding depth plane.
- `ONE_WAY`: write the plane only when the viewer is on the reverse side of
  that face's outward normal.
- `GLASS`: do not write an occluding plane.

The coremod transformer and depth pass compile and are included in the jar.
Runtime installation of the transformer and in-game visual behavior still need
client testing. The block's existing glass model has not yet been replaced by
mode-specific face rendering, so OPAQUE and MASK do not yet have their final
surface appearance. The per-block `maskDepth` setting is stored but is not yet
applied by the depth pass; the current scan is limited by the client's loaded
render distance. Edge cases such as overlapping Veil blocks, camera placement
inside a block, and exact ONE_WAY semantics also need in-game validation.

## Build

Use JDK 8 and Gradle 5.6.4, then run:

```bash
gradle build --no-daemon
```

The jar is created in `build/libs`. GitHub Actions also uploads it as the
`veilblock-forge-1.12.2` workflow artifact after a successful build.
