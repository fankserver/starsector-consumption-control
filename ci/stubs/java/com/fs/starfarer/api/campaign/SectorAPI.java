package com.fs.starfarer.api.campaign;
import com.fs.starfarer.api.EveryFrameScript;
import java.util.List;
public interface SectorAPI {
    CampaignFleetAPI getPlayerFleet();
    List<LocationAPI> getAllLocations();
    void addScript(EveryFrameScript script);
    void removeScriptsOfClass(Class<?> scriptClass);
}
