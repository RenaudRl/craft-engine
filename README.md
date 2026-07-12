# CraftEngine — BTC Fork

Fork de **[CraftEngine](https://github.com/Xiao-MoMi/craft-engine)** (Xiao-MoMi), adapté au serveur **BornToCraft** — Paper / Folia **26.2**. Base synchronisée sur l'upstream **26.7.2**.

## Nos ajouts / correctifs BTC
- **Hook de throttle de collision** (`CollisionUtils` via `BtcCoreHook`) — sous forte charge / régions denses en entités, la boucle de collision d'entités est court-circuitée via la façade `dev.btc.core.api.BTCCoreAPI` (`shouldCalculateCollision`). Les collisions de blocs restent respectées. Dégradation gracieuse : aucun effet hors BTC-CORE.

## Build
```bash
./gradlew :bukkit:build        # jar dans bukkit/build/libs/
```

---
Base upstream : `Xiao-MoMi/craft-engine` (26.7.2) · cible Minecraft **26.2**
