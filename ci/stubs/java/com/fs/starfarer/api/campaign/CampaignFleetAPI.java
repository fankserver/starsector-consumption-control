package com.fs.starfarer.api.campaign;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
public interface CampaignFleetAPI {
    FleetDataAPI getFleetData();
    FactionAPI getFaction();
    MutableFleetStatsAPI getStats();
    SectorEntityToken getInteractionTarget();
    void forceSync();
}
