package com.fs.starfarer.api.campaign;
import com.fs.starfarer.api.fleet.FleetDataAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
public interface CampaignFleetAPI {
    FleetDataAPI getFleetData();
    MutableFleetStatsAPI getStats();
    void forceSync();
}
