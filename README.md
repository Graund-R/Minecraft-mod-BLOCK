# Veil Block (Minecraft Forge 1.12.2)

A Forge 1.12.2 mod prototype for configurable optical masking blocks.

## Current prototype
- ForgeGradle 2.3 / Minecraft 1.12.2 / Java 8.
- Placeable Veil Block with a tile entity.
- Six independent face settings stored in the tile entity.
- **Sneak + left-click a face** to cycle its mode: Opaque -> Mask -> One-way window -> Glass.
- The client intercepts the interaction and sends a validated packet to the server.
- Face settings persist in the world and synchronize to clients.
- Default mask depth: 16 blocks (stored per block; render behavior is not connected yet).

## Rendering status
The actual per-viewer depth-mask renderer is not implemented yet. The current commit establishes the Forge project, block, face configuration, persistence, and interaction/network layer. The next stage is a client-side render pipeline that evaluates each block separately for each viewer, applies finite mask depth, and handles entities/particles as well as block geometry.

## Build
Use JDK 8 with Gradle 4.9 (compatible with ForgeGradle 2.3), then run:

```bash
gradle setupDecompWorkspace
gradle build
```

The resulting jar is in `build/libs`.