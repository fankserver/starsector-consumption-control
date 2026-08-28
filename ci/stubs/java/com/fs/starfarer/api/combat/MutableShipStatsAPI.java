package com.fs.starfarer.api.combat;
public interface MutableShipStatsAPI {
    MutableStat getSuppliesPerMonth();
    MutableStat getSuppliesToRecover();
    MutableStat getRepairRatePercentPerDay();
    MutableStat getBaseCRRecoveryRatePercentPerDay();
}
