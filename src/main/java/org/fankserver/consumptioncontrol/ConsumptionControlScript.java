package org.fankserver.consumptioncontrol;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ConsumptionControlScript implements EveryFrameScript {
    static final String MODIFIER_ID = "consumption_control";
    private static final String BUFF_ID = "consumption_control_ship_costs";
    private static final String DESCRIPTION = "Consumption Control";

    private transient Set<FleetMemberAPI> modifiedMembers;
    private transient Map<CampaignFleetAPI, FleetState> fleetStates;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) {
            return;
        }

        ConsumptionControlSettings.refresh();

        Set<CampaignFleetAPI> playerFleets = getPlayerFactionFleets();
        Set<FleetMemberAPI> playerMembers = identitySet();
        for (CampaignFleetAPI fleet : playerFleets) {
            playerMembers.addAll(fleet.getFleetData().getMembersListCopy());
        }
        removeBuffsFromDepartedMembers(playerMembers);
        removeModifiersFromDepartedFleets(playerFleets);

        for (CampaignFleetAPI fleet : playerFleets) {
            updateFleetIfNeeded(fleet);
        }
    }

    public void clearAll() {
        for (FleetMemberAPI member : modifiedMembers()) {
            removeBuff(member);
        }
        modifiedMembers().clear();

        for (CampaignFleetAPI fleet : fleetStates().keySet()) {
            fleet.forceSync();
            fleet.getStats().getFuelUseHyperMult().unmodify(MODIFIER_ID);
        }
        fleetStates().clear();
    }

    private void updateFleetIfNeeded(CampaignFleetAPI fleet) {
        boolean dockedAtMarket = isDockedAtMarket(fleet);
        Set<FleetMemberAPI> currentMembers = identitySet();
        currentMembers.addAll(fleet.getFleetData().getMembersListCopy());

        FleetState previous = fleetStates().get(fleet);
        if (previous != null
                && previous.settingsRevision == ConsumptionControlSettings.getRevision()
                && previous.members.equals(currentMembers)
                && previous.dockedAtMarket == dockedAtMarket) {
            return;
        }

        boolean applyShipAdjustments = ConsumptionControlSettings.isEnabled()
                && ConsumptionControlSettings.hasShipAdjustments(dockedAtMarket);
        for (FleetMemberAPI member : currentMembers) {
            if (applyShipAdjustments) {
                ensureBuff(member, dockedAtMarket);
                modifiedMembers().add(member);
            } else {
                removeBuff(member);
                modifiedMembers().remove(member);
            }
        }

        // Fleet synchronization resets ship stats, then reapplies buffs and updates repair rates.
        fleet.forceSync();

        fleet.getStats().getFuelUseHyperMult().unmodify(MODIFIER_ID);
        if (ConsumptionControlSettings.isEnabled()) {
            applyMultiplier(fleet.getStats().getFuelUseHyperMult(),
                    ConsumptionControlSettings.getHyperspaceFuelMultiplier());
        }

        fleetStates().put(fleet, new FleetState(currentMembers, dockedAtMarket,
                ConsumptionControlSettings.getRevision()));
    }

    private Set<CampaignFleetAPI> getPlayerFactionFleets() {
        Set<CampaignFleetAPI> fleets = identitySet();
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI fleet : location.getFleets()) {
                if (fleet.getFaction() != null && fleet.getFaction().isPlayerFaction()) {
                    fleets.add(fleet);
                }
            }
        }

        // Keep the directly controlled fleet covered during location transitions.
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet != null) {
            fleets.add(playerFleet);
        }
        return fleets;
    }

    private void ensureBuff(FleetMemberAPI member, boolean dockedAtMarket) {
        BuffManagerAPI manager = member.getBuffManager();
        BuffManagerAPI.Buff existing = manager.getBuff(BUFF_ID);
        if (!(existing instanceof ConsumptionControlBuff)
                || ((ConsumptionControlBuff) existing).dockedAtMarket != dockedAtMarket) {
            manager.removeBuff(BUFF_ID);
            manager.addBuff(new ConsumptionControlBuff(dockedAtMarket));
        }
    }

    private void removeBuffsFromDepartedMembers(Set<FleetMemberAPI> currentMembers) {
        Iterator<FleetMemberAPI> iterator = modifiedMembers().iterator();
        while (iterator.hasNext()) {
            FleetMemberAPI member = iterator.next();
            if (!currentMembers.contains(member)) {
                removeBuff(member);
                member.updateStats();
                iterator.remove();
            }
        }
    }

    private void removeModifiersFromDepartedFleets(Set<CampaignFleetAPI> currentFleets) {
        Iterator<CampaignFleetAPI> iterator = fleetStates().keySet().iterator();
        while (iterator.hasNext()) {
            CampaignFleetAPI fleet = iterator.next();
            if (!currentFleets.contains(fleet)) {
                fleet.getStats().getFuelUseHyperMult().unmodify(MODIFIER_ID);
                iterator.remove();
            }
        }
    }

    private void removeBuff(FleetMemberAPI member) {
        member.getBuffManager().removeBuff(BUFF_ID);
    }

    private static boolean isDockedAtMarket(CampaignFleetAPI fleet) {
        return fleet.getInteractionTarget() != null && fleet.getInteractionTarget().getMarket() != null;
    }

    private static void applyMultiplier(MutableStat stat, float multiplier) {
        if (Float.compare(multiplier, 1f) != 0) {
            stat.modifyMult(MODIFIER_ID, multiplier, DESCRIPTION);
        }
    }

    private Set<FleetMemberAPI> modifiedMembers() {
        if (modifiedMembers == null) {
            modifiedMembers = identitySet();
        }
        return modifiedMembers;
    }

    private Map<CampaignFleetAPI, FleetState> fleetStates() {
        if (fleetStates == null) {
            fleetStates = new IdentityHashMap<CampaignFleetAPI, FleetState>();
        }
        return fleetStates;
    }

    private static <T> Set<T> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());
    }

    private static final class FleetState {
        private final Set<FleetMemberAPI> members;
        private final boolean dockedAtMarket;
        private final long settingsRevision;

        private FleetState(Set<FleetMemberAPI> members, boolean dockedAtMarket, long settingsRevision) {
            this.members = members;
            this.dockedAtMarket = dockedAtMarket;
            this.settingsRevision = settingsRevision;
        }
    }

    public static final class ConsumptionControlBuff implements BuffManagerAPI.Buff {
        private final boolean dockedAtMarket;

        private ConsumptionControlBuff(boolean dockedAtMarket) {
            this.dockedAtMarket = dockedAtMarket;
        }

        @Override
        public void apply(FleetMemberAPI member) {
            applyMultiplier(member.getStats().getSuppliesPerMonth(),
                    ConsumptionControlSettings.getMaintenanceMultiplier());
            applyMultiplier(member.getStats().getSuppliesToRecover(),
                    ConsumptionControlSettings.getRecoverySupplyMultiplier(dockedAtMarket));
            applyMultiplier(member.getStats().getRepairRatePercentPerDay(),
                    ConsumptionControlSettings.getRepairSpeedMultiplier());
            applyMultiplier(member.getStats().getBaseCRRecoveryRatePercentPerDay(),
                    ConsumptionControlSettings.getCrRecoverySpeedMultiplier());
        }

        @Override
        public String getId() {
            return BUFF_ID;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void advance(float amount) {
        }
    }
}
