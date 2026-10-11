# Mekanism MoreMachine Version Locked — 2.0.0-port1

Minecraft 26.1.2 / NeoForge 26.1.2.109 / ATM11 0.9.0.

- Built against Mekanism Version Locked and its installed modules instead of embedding another Mekanism distribution.
- Preserved MoreMachine machines, advanced factories, large machines and generators.
- Fixed nested recipe output transactions and made generator/wireless transfers participate in an existing transaction when present.
- Fixed chance-based recycling operations incorrectly failing when the chance roll produces no item.
- Fixed missing face textures in lathing factories and large chemical infusers, and item model/particle definitions for large generators.
- Verified client/server connectivity, JEI startup and recycler/MoreMachine/Extras GUI loading.
- Verified 200 machine/factory tiles, including 100 Extras factories; tested recycler and basic/ultimate/absolute recycling factories with exact consumption and output.

Requires Mekanism Version Locked 2.3 for the custom-ingredient cache fix. Tested with Extras 1.4.2-fix5 and Additions/Generators/Tools 1.0.

Replicator and some general-purpose machine recipes remain configurable as in the original mod. No production features were removed by this port.

Original author: Lost Myself. Original source: https://github.com/lostmyself8/Mekanism-MoreMachine/tree/26.1 . MIT license and original credits preserved. This community build is not endorsed by the original developers.
