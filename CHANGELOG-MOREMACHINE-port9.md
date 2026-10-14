# Mekanism MoreMachine Version Locked 2.0.0-port9

- Fix large machines freezing with 128 speed upgrades: remove the raw 2^upgrade-count curve and overflowing int multiplication.
- Follow the installed Mekanism speed multiplier. With Version Locked core 2.8, 128 upgrades yield 65x speed and four base parallel operations yield a maximum of 260 operations/tick before energy, inputs and output capacity limit processing.
- Apply the same safe curve to large solar activators and advanced chemical washer/centrifuge factories. Solar throughput still depends on available sunlight and recipe resources.
- Scale large separator dumping to its live operation budget; clamp extreme operation arithmetic before converting to int.
- Charge energy for each actual parallel operation in all 26 powered cached-recipe paths, including MoreMachine and advanced factories, rather than allowing a batch for the cost of one operation.
- Add optional server settings largeMachineBaseOperations = 4, largeSolarBaseOperations = 1, and batchOperationsCost = true. Existing energy usage, storage, tier, generator and recipe config entries are retained. The batch charging setting requires a restart; false restores legacy charging.
- Retain the 64x large gas generator fuel balance, prior installer preservation, recipe compatibility, tank crash fixes and all-face connections.
- Validate on Minecraft 26.1.2 / NeoForge 26.1.2.109 with core2.8, Extras fix6 and Generators1.3 using actual server recipes at 0/1/8/32/64/128 speed upgrades, no-energy guards, input/output conservation, partial energy and full-output tests.

