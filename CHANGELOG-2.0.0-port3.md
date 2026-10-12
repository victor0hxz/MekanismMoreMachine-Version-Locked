# Mekanism MoreMachine Version Locked — 2.0.0-port3

- Accept the legacy `item`/`tag` ingredient JSON used by ATM11 KubeJS scripts and optional processor integrations, alongside the modern nested ingredient format.
- Preserve required input quantities and reject invalid quantities; molds without an explicit quantity default to one.
- Apply compatibility to stamping, pressing, recycling and planting serializers; retain the existing network format.
- Fix the ingredient-format failures reported for six optional processor recipes and ATM11 stamping recipes.
- Add a Version Locked logo and illustrated project description, preserving the original author credits and MIT license.

Requires Mekanism Version Locked 2.4 or newer. Minecraft 26.1.2 / NeoForge / ATM11 0.9.0. Install matching versions on clients and server.

This update does not claim to fix the separate server watchdog timeout during world saving.
