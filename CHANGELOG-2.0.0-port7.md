# Mekanism MoreMachine Version Locked — 2.0.0-port7

- Increase the large gas generator's maximum fuel burn rate with `gas_generator.largeGasBurnRateMultiplier`, default 64, in `config/MekanismMoreMachine/generators.toml`.
- With the supplied ATM11 ethene values (6,000 FE/mB, base maximum 12 mB/t), the large generator can burn 768 mB/t and generate 4.608 MFE/t with a full tank and available buffer/output capacity. Fuel energy per mB remains unchanged.
- Keep burn rate, automatic energy output limit, actual generation tracking and UI readings consistent. Use saturating arithmetic and cap per-tick generation/output at the transfer API's integer limit.
- Add English/Portuguese fuel-gauge tooltips showing the large machine's effective burn and output limits, distinct from the base chemical fuel values.
- Setting the multiplier to 1 restores the previous rate. Retain all port6 generation fixes, all-face large processing machine connections, dumping fixes, molds and recipe compatibility.

Validation: dedicated-server tests verify maximum generation from a full tank, 64x throughput, fuel-energy conservation, actual reported energy, external extraction/rollback and zero fuel consumption with a full buffer. Existing machine/factory, recycling, connection and recipe tests pass.

Update matching clients and server. Existing fuel and stored energy are preserved. Minecraft 26.1.2 / NeoForge / Java 25 / ATM11 0.9.0. Original MoreMachine credits and MIT license preserved.
