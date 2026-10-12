# Mekanism MoreMachine Version Locked — 2.0.0-port6

- Fix the large gas-burning generator's energy tab: display actual FE generated, rather than the mB of fuel burned mislabeled as FE.
- Track the amount of energy committed by each generation transaction, avoiding overstatement when the buffer has only partial room.
- Initialize and synchronize a non-zero energy output rate from the current fuel's energy density and maximum burn rate. Automatic output no longer has a zero transfer limit.
- Retain port5 large-machine all-face connections and chemical dumping fixes, port4 craftable molds and previous recipe compatibility.

Validation: dedicated-server checks verify real generation, correct reported energy, a positive output rate, external extraction and rollback, fuel energy conservation, and no fuel consumption/phantom output with a full energy buffer. Existing machine/factory, connection, recipe and recycling tests pass.

Update clients and server together and replace the old MoreMachine JAR. Minecraft 26.1.2 / NeoForge / Java 25 / ATM11 0.9.0. Mekanism 2.7 and Generators 1.1 are recommended for the accompanying energy and visual fixes. Original MoreMachine credits and MIT license preserved.
