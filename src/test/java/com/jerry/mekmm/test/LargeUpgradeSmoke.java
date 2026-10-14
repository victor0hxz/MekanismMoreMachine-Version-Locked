package com.jerry.mekmm.test;

import mekanism.api.Upgrade;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

final class LargeUpgradeSmoke {
    static void run(ServerLevel level) {
        for (int speed : new int[]{0, 1, 8, 32, 64, 128}) {
            var pos = new BlockPos(960 + speed * 3, 150, 300);
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:large_electrolytic_separator")).defaultBlockState());
            var tile = (com.jerry.meklm.common.tile.machine.TileEntityLargeElectrolyticSeparator) level.getBlockEntity(pos);
            tile.getComponent().addUpgrades(Upgrade.SPEED, speed);
            tile.recalculateUpgrades(Upgrade.SPEED);
            var energy = (MachineEnergyContainer<?>) tile.getEnergyContainer();
            long perOperation = energy.getEnergyPerTick();
            MoreMachineSmoke.check(perOperation > 0, "separator positive per-operation energy " + speed);
            tile.fluidTank.setContents(FluidResource.of(Fluids.WATER), 1000, null);
            energy.setEnergy(0, null);
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            MoreMachineSmoke.check(tile.fluidTank.amountAsLong() == 1000 && tile.leftTank.isEmpty() && tile.rightTank.isEmpty(), "separator cannot process without energy " + speed);
            energy.setEnergy(energy.getCapacityAsLong(), null);
            long energyBefore = energy.getAmountAsLong();
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            long consumed = 1000 - tile.fluidTank.amountAsLong();
            long expected = Math.min(500, Math.min((int) (4 * mekanism.common.util.MekanismUtils.getUpgradeMultiplier(tile, Upgrade.SPEED)), energyBefore / perOperation));
            MoreMachineSmoke.check(consumed / 2 == expected && consumed > 0, "separator follows actual Mekanism throughput at speed " + speed);
            MoreMachineSmoke.check(tile.leftTank.amountAsLong() == consumed && tile.rightTank.amountAsLong() == consumed / 2, "separator conserves recipe stoichiometry " + speed);
            MoreMachineSmoke.check(energyBefore - energy.getAmountAsLong() == (consumed / 2) * perOperation, "separator charges every actual operation " + speed);
            tile.leftTank.setContents(tile.leftTank.resource(), 1024, null);
            tile.rightTank.setContents(tile.rightTank.resource(), 1024, null);
            tile.fluidTank.setContents(FluidResource.of(Fluids.WATER), 0, null);
            long output = tile.leftTank.amountAsLong() + tile.rightTank.amountAsLong();
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            MoreMachineSmoke.check(tile.leftTank.amountAsLong() + tile.rightTank.amountAsLong() == output, "separator cannot manufacture output without input " + speed);
        }
        checkMix(level, "large_chemical_infuser", "leftTank", "rightTank", "centerTank", "hydrogen", "chlorine", "hydrogen_chloride", 1, 1);
        checkMix(level, "large_pigment_mixer", "leftInputTank", "rightInputTank", "outputTank", "blue", "red", "purple", 2, 2);
        checkRotary(level);
        MoreMachineSmoke.check(com.jerry.meklm.common.util.LargeMachineOperations.multiply(Integer.MAX_VALUE, 65) == Integer.MAX_VALUE, "large operations saturate instead of overflowing");
        MoreMachineSmoke.check(com.jerry.meklm.common.util.LargeMachineOperations.multiply(65, 0) == 0, "solar darkness yields zero operations");
        MoreMachineSmoke.check(com.jerry.meklm.common.util.LargeMachineOperations.multiply(65, Double.NaN) == 0, "invalid solar rate cannot yield operations");
        MoreMachineSmoke.check(com.jerry.meklm.common.util.LargeMachineOperations.multiply(65, Double.POSITIVE_INFINITY) == Integer.MAX_VALUE, "extreme solar rate safely saturates");
        int factoryIndex = 0;
        for (String name : new String[]{"basic_centrifuging_factory", "ultimate_centrifuging_factory", "basic_washing_factory", "ultimate_washing_factory"}) {
            var pos = new BlockPos(1900 + factoryIndex++ * 5, 150, 300);
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:" + name)).defaultBlockState());
            var tile = (TileEntityMekanism) level.getBlockEntity(pos);
            MoreMachineSmoke.check(tile != null, name + " exists");
            tile.getComponent().addUpgrades(Upgrade.SPEED, 128);
            try {
                var field = com.jerry.mekaf.common.tile.factory.base.TileEntityChemicalToChemicalFactory.class.getDeclaredField("baselineMaxOperations");
                field.setAccessible(true);
                MoreMachineSmoke.check(field.getInt(tile) == 65, name + " follows Mekanism baseline at128 speed: " + field.getInt(tile));
                if (name.contains("centrifuging")) {
                    var inputField = com.jerry.mekaf.common.tile.factory.base.TileEntityChemicalToChemicalFactory.class.getDeclaredField("inputChemicalTanks");
                    var outputField = com.jerry.mekaf.common.tile.factory.base.TileEntityChemicalToChemicalFactory.class.getDeclaredField("outputChemicalTanks");
                    inputField.setAccessible(true);
                    outputField.setAccessible(true);
                    var inputs = (java.util.List<mekanism.api.chemical.IChemicalTank>) inputField.get(tile);
                    var outputs = (java.util.List<mekanism.api.chemical.IChemicalTank>) outputField.get(tile);
                    inputs.getFirst().setContents(chemical("uranium_hexafluoride"), 1000, null);
                    var energy = (MachineEnergyContainer<?>) tile.getEnergyContainer();
                    energy.setEnergy(0, null);
                    TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
                    MoreMachineSmoke.check(inputs.getFirst().amountAsLong() == 1000, name + " cannot convert without energy");
                    energy.setEnergy(energy.getCapacityAsLong(), null);
                    long before = energy.getAmountAsLong();
                    long cost = energy.getEnergyPerTick();
                    TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
                    long consumed = 1000 - inputs.stream().mapToLong(t -> t.amountAsLong()).sum();
                    long produced = outputs.stream().filter(t -> t.resource().equals(chemical("fissile_fuel"))).mapToLong(t -> t.amountAsLong()).sum();
                    MoreMachineSmoke.check(consumed > 0 && produced == consumed && consumed <= 65, name + " converts within Mekanism operation limit");
                    MoreMachineSmoke.check(before - energy.getAmountAsLong() == consumed * cost, name + " pays for every real conversion");
                }
            } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
        }
        int index = 0;
        for (String name : new String[]{"large_electrolytic_separator", "large_rotary_condensentrator", "large_chemical_infuser", "large_pigment_mixer", "large_solar_neutron_activator"}) {
            var pos = new BlockPos(900 + index++ * 5, 150, 300);
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:" + name)).defaultBlockState());
            var tile = (TileEntityMekanism) level.getBlockEntity(pos);
            tile.getComponent().addUpgrades(Upgrade.SPEED, 128);
            MoreMachineSmoke.check(tile.getUpgrades(Upgrade.SPEED) == 128, name + " accepts 128 speed upgrades");
            tile.recalculateUpgrades(Upgrade.SPEED);
            int base = name.equals("large_solar_neutron_activator") ? 1 : 4;
            int operations = com.jerry.meklm.common.util.LargeMachineOperations.forTile(tile, base);
            MoreMachineSmoke.check(operations == base * 65, name + " uses bounded Mekanism speed throughput: " + operations);

        }
    }
    private static mekanism.api.chemical.IChemicalTank tank(TileEntityMekanism tile, String name) {
        try { return (mekanism.api.chemical.IChemicalTank) tile.getClass().getField(name).get(tile); }
        catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }

    private static mekanism.api.chemical.ChemicalResource chemical(String name) {
        return mekanism.api.chemical.ChemicalResource.of(mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.getValue(Identifier.parse("mekanism:" + name)));
    }

    private static void checkMix(ServerLevel level, String machine, String leftName, String rightName,
            String outputName, String leftChemical, String rightChemical, String outputChemical, int yield, int row) {
        for (int speed : new int[]{0, 1, 8, 32, 64, 128}) {
            var pos = new BlockPos(1300 + speed * 3, 150, 300 + row * 5);
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:" + machine)).defaultBlockState());
            var tile = (TileEntityMekanism) level.getBlockEntity(pos);
            tile.getComponent().addUpgrades(Upgrade.SPEED, speed);
            var left = tank(tile, leftName);
            var right = tank(tile, rightName);
            var output = tank(tile, outputName);
            var energy = (MachineEnergyContainer<?>) tile.getEnergyContainer();
            long cost = energy.getEnergyPerTick();
            left.setContents(chemical(leftChemical), 1000, null);
            right.setContents(chemical(rightChemical), 1000, null);
            energy.setEnergy(0, null);
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            MoreMachineSmoke.check(left.amountAsLong() == 1000 && right.amountAsLong() == 1000 && output.isEmpty(), machine + " cannot process without energy " + speed);
            energy.setEnergy(energy.getCapacityAsLong(), null);
            long before = energy.getAmountAsLong();
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            long consumed = 1000 - left.amountAsLong();
            long expected = Math.min(1000, Math.min((int) (4 * mekanism.common.util.MekanismUtils.getUpgradeMultiplier(tile, Upgrade.SPEED)), before / cost));
            MoreMachineSmoke.check(consumed == expected && consumed > 0 && 1000 - right.amountAsLong() == consumed && output.amountAsLong() == consumed * yield && output.resource().equals(chemical(outputChemical)), machine + " conserves recipe resources " + speed);
            MoreMachineSmoke.check(before - energy.getAmountAsLong() == consumed * cost, machine + " charges actual operations " + speed);
            left.setContents(chemical(leftChemical), 1000, null);
            right.setContents(chemical(rightChemical), 1000, null);
            output.setContents(chemical(outputChemical), 0, null);
            energy.setEnergy(cost, null);
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            MoreMachineSmoke.check(left.amountAsLong() == 999 && right.amountAsLong() == 999 && output.amountAsLong() == yield && energy.getAmountAsLong() == 0, machine + " limits processing to one paid operation " + speed);
            output.setContents(chemical(outputChemical), output.capacityAsLong(chemical(outputChemical)), null);
            energy.setEnergy(energy.getCapacityAsLong(), null);
            before = energy.getAmountAsLong();
            TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
            MoreMachineSmoke.check(left.amountAsLong() == 999 && right.amountAsLong() == 999 && energy.getAmountAsLong() == before, machine + " full output blocks resource and energy use " + speed);
        }
    }

    private static void checkRotary(ServerLevel level) {
        for (int speed : new int[]{0, 1, 8, 32, 64, 128}) {
            var pos = new BlockPos(1300 + speed * 3, 150, 320);
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:large_rotary_condensentrator")).defaultBlockState());
            var tile = (com.jerry.meklm.common.tile.machine.TileEntityLargeRotaryCondensentrator) level.getBlockEntity(pos);
            tile.getComponent().addUpgrades(Upgrade.SPEED, speed);
            var oxygen = chemical("oxygen");
            var liquid = FluidResource.of(BuiltInRegistries.FLUID.getValue(Identifier.parse("mekanism:oxygen")));
            var energy = (MachineEnergyContainer<?>) tile.getEnergyContainer();
            long cost = energy.getEnergyPerTick();
            for (boolean evaporate : new boolean[]{false, true}) {
                if (tile.getMode() != evaporate) tile.nextMode();
                tile.chemicalTank.setContents(oxygen, evaporate ? 0 : 1000, null);
                tile.fluidTank.setContents(liquid, evaporate ? 1000 : 0, null);
                energy.setEnergy(0, null);
                TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
                MoreMachineSmoke.check(tile.chemicalTank.amountAsLong() == (evaporate ? 0 : 1000) && tile.fluidTank.amountAsLong() == (evaporate ? 1000 : 0), "rotary no energy blocks both modes " + speed + "/" + evaporate);
                energy.setEnergy(energy.getCapacityAsLong(), null);
                long before = energy.getAmountAsLong();
                TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
                long produced = evaporate ? tile.chemicalTank.amountAsLong() : tile.fluidTank.amountAsLong();
                MoreMachineSmoke.check(produced > 0 && tile.chemicalTank.amountAsLong() + tile.fluidTank.amountAsLong() == 1000, "rotary conserves conversion in both modes " + speed + "/" + evaporate);
                MoreMachineSmoke.check(before - energy.getAmountAsLong() == produced * cost, "rotary charges actual conversion operations " + speed + "/" + evaporate);
            }
        }
    }
}







