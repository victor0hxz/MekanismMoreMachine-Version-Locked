package com.jerry.mekmm.test;

import com.jerry.meklg.common.registries.LargeGeneratorBlocks;
import com.jerry.meklg.common.tile.generator.TileEntityLargeGasGenerator;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class LargeGasGeneratorSmoke {
    static void run(ServerLevel level) {
        var pos = new BlockPos(800, 150, 300);
        level.setBlockAndUpdate(pos, LargeGeneratorBlocks.LARGE_GAS_BURNING_GENERATOR.get().defaultBlockState());
        var tile = (TileEntityLargeGasGenerator) level.getBlockEntity(pos);
        var fuel = ChemicalResource.of(MekanismChemicals.ETHENE);
        tile.fuelTank.setContents(fuel, tile.fuelTank.capacityAsLong(fuel), null);
        tile.getEnergyContainer().setEnergy(0, null);
        long fuelBefore = tile.fuelTank.amountAsLong();
        TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
        long generated = tile.getEnergyContainer().getAmountAsLong();
        MoreMachineSmoke.check(tile.getMaximumBurnRate() == 64L * tile.getCachedFuel().maxBurnPerTick(), "large generator scales fuel throughput 64 times");
        MoreMachineSmoke.check(generated == Math.min(tile.getEnergyContainer().getCapacityAsLong(), tile.getMaximumBurnRate() * tile.getCachedFuel().energyDensity()), "full-fuel generator reaches its configured maximum power");
        MoreMachineSmoke.check(generated > 0, "large gas generator creates actual energy");
        MoreMachineSmoke.check(tile.getProductionRate() == generated, "gas generator reports FE produced rather than mB burned");
        MoreMachineSmoke.check(tile.getMaxOutput() > 0, "gas generator has a non-zero energy output rate");
        MoreMachineSmoke.check(generated <= (fuelBefore - tile.fuelTank.amountAsLong()) * tile.getCachedFuel().energyDensity(), "gas energy does not exceed energy in consumed fuel");
        try (var tx = Transaction.openRoot()) {
            MoreMachineSmoke.check(tile.getEnergyContainer().extract((int) Math.min(generated, Integer.MAX_VALUE), tx, AutomationType.EXTERNAL) > 0,
                    "generated energy can be extracted externally");
        }
        MoreMachineSmoke.check(tile.getEnergyContainer().getAmountAsLong() == generated, "energy extraction simulation rolls back");
        tile.getEnergyContainer().setEnergy(tile.getEnergyContainer().getCapacityAsLong(), null);
        fuelBefore = tile.fuelTank.amountAsLong();
        TileEntityMekanism.tickServer(level, pos, tile.getBlockState(), tile);
        MoreMachineSmoke.check(tile.getProductionRate() == 0 && tile.fuelTank.amountAsLong() == fuelBefore,
                "full generator buffer does not consume fuel or report phantom energy");
    }
}
