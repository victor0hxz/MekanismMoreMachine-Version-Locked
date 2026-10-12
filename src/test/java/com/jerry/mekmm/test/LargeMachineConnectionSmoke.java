package com.jerry.mekmm.test;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class LargeMachineConnectionSmoke {
    static void run(ServerLevel level) {
        int index = 0;
        for (String name : new String[]{"large_electrolytic_separator", "large_rotary_condensentrator",
                "large_chemical_infuser", "large_antiprotonic_nucleosynthesizer", "large_pigment_mixer",
                "large_solar_neutron_activator"}) {
            var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:" + name));
            for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                var pos = new BlockPos(300 + index++ * 5, 150, 300);
                var state = mekanism.common.block.attribute.Attribute.setFacing(block.defaultBlockState(), facing);
                level.setBlockAndUpdate(pos, state);
                var tile = (TileEntityMekanism) level.getBlockEntity(pos);
                MoreMachineSmoke.check(tile instanceof IBoundingBlock, name + " bounding tile exists");
                var bounding = (IBoundingBlock) tile;
                for (Direction side : Direction.values()) {
                    var offset = new Vec3i(0, 1, 0);
                    var energy = bounding.getOffsetCapability(Capabilities.ENERGY.block(), side, offset);
                    if (!name.equals("large_solar_neutron_activator")) {
                        MoreMachineSmoke.check(energy != null, name + " energy accessible " + facing + "/" + side);
                        tile.getEnergyContainer().setEnergy(0, null);
                        try (var tx = Transaction.openRoot()) {
                            MoreMachineSmoke.check(energy.insert(10, tx) == 10, name + " accepts energy " + side);
                            tx.commit();
                        }
                        MoreMachineSmoke.check(tile.getEnergyContainer().getAmountAsLong() == 10, name + " preserves energy amount");
                    }
                    var chemical = bounding.getOffsetCapability(Capabilities.CHEMICAL.block(), side, offset);
                    MoreMachineSmoke.check(chemical != null && chemical.size() > 0, name + " chemical tanks accessible " + facing + "/" + side);
                    if (name.equals("large_electrolytic_separator")) {
                        var fluid = bounding.getOffsetCapability(Capabilities.FLUID.block(), side, offset);
                        MoreMachineSmoke.check(fluid != null && fluid.size() > 0, "separator fluid accessible " + facing + "/" + side);
                        try (var tx = Transaction.openRoot()) {
                            MoreMachineSmoke.check(fluid.insert(FluidResource.of(Fluids.WATER), 100, tx) == 100, "separator accepts water " + side);
                            MoreMachineSmoke.check(fluid.extract(FluidResource.of(Fluids.WATER), 100, tx) == 0, "separator input cannot be drained");
                        }
                        MoreMachineSmoke.check(tile.getFluidTanks().getFirst().isEmpty(), "water simulation rolls back");
                    }
                }
            }
        }
        var oxygen = mekanism.api.chemical.ChemicalResource.of(mekanism.common.registries.MekanismChemicals.OXYGEN);
        var separatorPos = new BlockPos(550, 150, 250);
        level.setBlockAndUpdate(separatorPos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:large_electrolytic_separator")).defaultBlockState());
        var separator = (com.jerry.meklm.common.tile.machine.TileEntityLargeElectrolyticSeparator) level.getBlockEntity(separatorPos);
        separator.dumpLeft = mekanism.common.tile.TileEntityChemicalTank.GasMode.DUMPING;
        separator.leftTank.setContents(oxygen, 1, null);
        TileEntityMekanism.tickServer(level, separatorPos, separator.getBlockState(), separator);
        MoreMachineSmoke.check(separator.leftTank.isEmpty(), "large separator safely dumps final chemical unit");
        var rotaryBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:large_rotary_condensentrator"));
        var rotaryPos = new BlockPos(550, 150, 300);
        level.setBlockAndUpdate(rotaryPos, rotaryBlock.defaultBlockState());
        var rotary = (com.jerry.meklm.common.tile.machine.TileEntityLargeRotaryCondensentrator) level.getBlockEntity(rotaryPos);
        try {
            var mode = rotary.getClass().getDeclaredField("mode");
            mode.setAccessible(true);
            for (boolean fluidToChemical : new boolean[]{false, true}) {
                mode.setBoolean(rotary, fluidToChemical);
                for (Direction side : Direction.values()) {
                    var gas = rotary.getOffsetCapability(Capabilities.CHEMICAL.block(), side, new Vec3i(0, 1, 0));
                    rotary.chemicalTank.setContents(oxygen, 100, null);
                    try (var tx = Transaction.openRoot()) {
                        MoreMachineSmoke.check(gas.extract(oxygen, 10, tx) == (fluidToChemical ? 10 : 0), "rotary preserves mode-specific gas output on " + side);
                    }
                    rotary.chemicalTank.setContents(oxygen, 0, null);
                    try (var tx = Transaction.openRoot()) {
                        MoreMachineSmoke.check(gas.insert(oxygen, 10, tx) == (fluidToChemical ? 0 : 10), "rotary preserves mode-specific gas input on " + side);
                    }
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
        for (String name : new String[]{"ultimate_max_chemical_tank", "ultimate_mid_chemical_tank"}) {
            var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse("mekmm:" + name));
            var pos = new BlockPos(600 + index++ * 5, 150, 300);
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            var tile = (com.jerry.meklm.common.tile.prefab.TileEntityLargeChemicalTank) level.getBlockEntity(pos);
            var tank = tile.getChemicalTanks().getFirst();
            tile.dumping = mekanism.common.tile.TileEntityChemicalTank.GasMode.DUMPING;
            for (long amount : new long[]{1, 105, 10_000, 0}) {
                tank.setContents(oxygen, amount, null);
                TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
                MoreMachineSmoke.check(tank.isEmpty(), name + " safely dumps remaining " + amount + " without negative contents");
            }
        }
        com.jerry.mekmm.common.config.MoreMachineConfig.general.largeMachineAllFaces.set(false);
        try {
            var pos = new BlockPos(550, 150, 350);
            level.setBlockAndUpdate(pos, rotaryBlock.defaultBlockState());
            var originalPorts = (IBoundingBlock) level.getBlockEntity(pos);
            MoreMachineSmoke.check(originalPorts.getOffsetCapability(Capabilities.ENERGY.block(), Direction.UP, new Vec3i(0, 1, 0)) == null,
                    "disabling all-faces restores colored-port restriction");
        } finally {
            com.jerry.mekmm.common.config.MoreMachineConfig.general.largeMachineAllFaces.set(true);
        }
    }
}
