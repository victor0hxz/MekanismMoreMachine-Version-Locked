package com.jerry.meklm.common.util;

import mekanism.api.Upgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.MekanismUtils;

/** Throughput follows the installed Mekanism speed curve and saturates before conversion. */
public final class LargeMachineOperations {
    private LargeMachineOperations() { }

    public static int forTile(IUpgradeTile tile, int baseOperations) {
        return multiply(baseOperations, MekanismUtils.getUpgradeMultiplier(tile, Upgrade.SPEED));
    }

    public static int multiply(int operations, double multiplier) {
        if (operations <= 0 || !(multiplier > 0)) {
            return 0;
        }
        double result = operations * multiplier;
        return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
}
