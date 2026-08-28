package com.fs.starfarer.api.fleet;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
public interface FleetMemberAPI {
    MutableShipStatsAPI getStats();
    BuffManagerAPI getBuffManager();
    void updateStats();
}
