package com.fs.starfarer.api.campaign;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.List;
public interface FleetDataAPI {
    List<FleetMemberAPI> getMembersListCopy();
}
