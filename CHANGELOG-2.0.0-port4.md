# Mekanism MoreMachine Version Locked — 2.0.0-port4

- Add craftable gear, plate and rod molds (`mekmm:mold_gear`, `mekmm:mold_plate`, `mekmm:mold_rod`) for installations without Immersive Engineering. Craft them with steel ingots; the Stamper retains the molds during processing.
- Resolve only the three missing Immersive Engineering mold IDs in Stamper recipe JSON to the corresponding MoreMachine items. Registered original molds retain their original behavior. Unrelated missing items remain errors.
- Add item models using existing vanilla assets, English and Portuguese names, and crafting recipes.
- Preserve the legacy and modern ingredient support from port3.

Validation: ingredient and mold codec checks passed, including wrong-item and unknown-ID rejection. Existing machine/factory functional tests passed. A full archived ATM11 server started and completed `save-all flush` without MoreMachine recipe parsing errors.

Install the same version on clients and server. Back up and replace older MoreMachine JARs. Requires Minecraft 26.1.2, Java 25, NeoForge and Mekanism Version Locked 2.4 or newer.

This release corrects compatibility and recipes. It does not claim a measured TPS improvement or modify other mods. Server configuration and pack-specific KubeJS fuel migration are separate from this JAR.
