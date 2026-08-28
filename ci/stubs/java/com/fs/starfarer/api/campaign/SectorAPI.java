package com.fs.starfarer.api.campaign;
import com.fs.starfarer.api.EveryFrameScript;
public interface SectorAPI {
    CampaignFleetAPI getPlayerFleet();
    void addScript(EveryFrameScript script);
    void removeScriptsOfClass(Class<?> scriptClass);
}
