package org.fankserver.consumptioncontrol;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public final class ConsumptionControlScript implements EveryFrameScript {
    static final String MODIFIER_ID = "consumption_control";
    private static final String DESCRIPTION = "Consumption Control";

    private transient Set<FleetMemberAPI> modifiedMembers;
    private transient Set<FleetMemberAPI> lastPlayerFleetMembers;
    private transient long appliedSettingsRevision = Long.MIN_VALUE;

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
        CampaignFleetAPI fleet = Global.getSector() == null ? null : Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return;
        }

        Set<FleetMemberAPI> currentMembers = identitySet();
        currentMembers.addAll(fleet.getFleetData().getMembersListCopy());
        if (appliedSettingsRevision == ConsumptionControlSettings.getRevision()
                && currentMembers.equals(lastPlayerFleetMembers())) {
            return;
        }

        clearDepartedMembers(currentMembers);
        clearFleetModifier(fleet);
        clearMembers(currentMembers);

        if (ConsumptionControlSettings.isEnabled()) {
            applyMultiplier(fleet.getStats().getFuelUseHyperMult(),
                    ConsumptionControlSettings.getHyperspaceFuelMultiplier());
            for (FleetMemberAPI member : currentMembers) {
                applyToMember(member);
                modifiedMembers().add(member);
            }
        } else {
            modifiedMembers().clear();
        }

        lastPlayerFleetMembers().clear();
        lastPlayerFleetMembers().addAll(currentMembers);
        appliedSettingsRevision = ConsumptionControlSettings.getRevision();
    }

    public void clearAll() {
        if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            clearFleetModifier(fleet);
            clearMembers(fleet.getFleetData().getMembersListCopy());
        }
        clearMembers(modifiedMembers());
        modifiedMembers().clear();
        lastPlayerFleetMembers().clear();
        appliedSettingsRevision = Long.MIN_VALUE;
    }

    private void applyToMember(FleetMemberAPI member) {
        applyMultiplier(member.getStats().getSuppliesPerMonth(),
                ConsumptionControlSettings.getMaintenanceMultiplier());
        applyMultiplier(member.getStats().getSuppliesToRecover(),
                ConsumptionControlSettings.getRecoverySupplyMultiplier());
        applyMultiplier(member.getStats().getRepairRatePercentPerDay(),
                ConsumptionControlSettings.getRepairSpeedMultiplier());
        applyMultiplier(member.getStats().getBaseCRRecoveryRatePercentPerDay(),
                ConsumptionControlSettings.getCrRecoverySpeedMultiplier());
    }

    private void clearDepartedMembers(Set<FleetMemberAPI> currentMembers) {
        Iterator<FleetMemberAPI> iterator = modifiedMembers().iterator();
        while (iterator.hasNext()) {
            FleetMemberAPI member = iterator.next();
            if (!currentMembers.contains(member)) {
                clearMember(member);
                iterator.remove();
            }
        }
    }

    private void applyMultiplier(MutableStat stat, float multiplier) {
        if (Float.compare(multiplier, 1f) != 0) {
            stat.modifyMult(MODIFIER_ID, multiplier, DESCRIPTION);
        }
    }

    private void clearFleetModifier(CampaignFleetAPI fleet) {
        fleet.getStats().getFuelUseHyperMult().unmodify(MODIFIER_ID);
    }

    private void clearMembers(Iterable<FleetMemberAPI> members) {
        for (FleetMemberAPI member : members) {
            clearMember(member);
        }
    }

    private void clearMember(FleetMemberAPI member) {
        member.getStats().getSuppliesPerMonth().unmodify(MODIFIER_ID);
        member.getStats().getSuppliesToRecover().unmodify(MODIFIER_ID);
        member.getStats().getRepairRatePercentPerDay().unmodify(MODIFIER_ID);
        member.getStats().getBaseCRRecoveryRatePercentPerDay().unmodify(MODIFIER_ID);
    }

    private Set<FleetMemberAPI> modifiedMembers() {
        if (modifiedMembers == null) {
            modifiedMembers = identitySet();
        }
        return modifiedMembers;
    }

    private Set<FleetMemberAPI> lastPlayerFleetMembers() {
        if (lastPlayerFleetMembers == null) {
            lastPlayerFleetMembers = identitySet();
        }
        return lastPlayerFleetMembers;
    }

    private static Set<FleetMemberAPI> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<FleetMemberAPI, Boolean>());
    }
}
