# Mekanism MoreMachine Version Locked — 2.0.0-port5

- Fix a dedicated-server crash when a large chemical tank in dumping mode contains less chemical than its per-tick dumping rate. Clamp the discarded amount to the stored amount, preventing negative contents. Existing tanks do not need to be removed.
- Apply the same final-unit protection to the large electrolytic separator.
- Allow energy, fluids and chemicals through all faces and bounding parts of the six large processing machines: electrolytic separator, rotary condensentrator, chemical infuser, antiprotonic nucleosynthesizer, pigment mixer and solar neutron activator. Only resource types supported by each machine are exposed.
- Preserve recipe validation, input-only/output-only tank rules, shared storage and rotary operation modes. Inventory automation and automatic output locations retain their existing behavior.
- Add `largeMachineAllFaces = true` in `config/MekanismMoreMachine/general.toml`. Set it to `false` and restart to restore the original colored-port restrictions. Smaller machines, storage tank port layouts and generators retain their existing behavior.
- Retain port4 craftable molds, ingredient compatibility and all existing machine/factory features.

Validation: dedicated-server functional checks passed for 200 machine/factory tiles, including Mekanism Extras factories, recycler item conservation and legacy/modern recipes. New checks cover all six faces in four orientations, energy insertion, water insertion and rollback, input tank extraction rejection, both rotary chemical modes, final-unit separator dumping, large tank dumping with 0/1/105/10000 units, and restoration of the original port restrictions.

Install this version on both clients and server. Stop the game/server, back up and replace the previous MoreMachine JAR; keep only one MoreMachine version. Requires Minecraft 26.1.2, Java 25, NeoForge and Mekanism Version Locked 2.4 or newer. Mekanism 2.6 is recommended for its separate electrolytic separator energy-capacity fix. This release modifies only MoreMachine and does not claim a measured TPS improvement.
