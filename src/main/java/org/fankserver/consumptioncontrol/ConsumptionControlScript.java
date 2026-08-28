package org.fankserver.consumptioncontrol;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public final class ConsumptionControlScript implements EveryFrameScript {
    static final String MODIFIER_ID = "consumption_control";
    private static final String BUFF_ID = "consumption_control_ship_costs";
    private static final String DESCRIPTION = "Consumption Control";

    private transient Set<FleetMemberAPI> modifiedMembers;
    private transient Set<FleetMemberAPI> lastPlayerFleetMembers;
    private transient Boolean lastDockedAtMarket;
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

        ConsumptionControlSettings.refresh();

        boolean dockedAtMarket = isDockedAtMarket(fleet);
        Set<FleetMemberAPI> currentMembers = identitySet();
        currentMembers.addAll(fleet.getFleetData().getMembersListCopy());
        if (appliedSettingsRevision == ConsumptionControlSettings.getRevision()
                && currentMembers.equals(lastPlayerFleetMembers())
                && lastDockedAtMarket != null
                && lastDockedAtMarket == dockedAtMarket) {
            return;
        }

        removeBuffsFromDepartedMembers(currentMembers);
        boolean applyShipAdjustments = ConsumptionControlSettings.isEnabled()
                && ConsumptionControlSettings.hasShipAdjustments(dockedAtMarket);

        for (FleetMemberAPI member : currentMembers) {
            if (applyShipAdjustments) {
                ensureBuff(member);
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

        lastPlayerFleetMembers().clear();
        lastPlayerFleetMembers().addAll(currentMembers);
        lastDockedAtMarket = dockedAtMarket;
        appliedSettingsRevision = ConsumptionControlSettings.getRevision();
    }

    public void clearAll() {
        CampaignFleetAPI fleet = Global.getSector() == null ? null : Global.getSector().getPlayerFleet();
        if (fleet != null) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                removeBuff(member);
            }
        }
        for (FleetMemberAPI member : modifiedMembers()) {
            removeBuff(member);
        }
        modifiedMembers().clear();

        if (fleet != null) {
            fleet.forceSync();
            fleet.getStats().getFuelUseHyperMult().unmodify(MODIFIER_ID);
        }

        lastPlayerFleetMembers().clear();
        lastDockedAtMarket = null;
        appliedSettingsRevision = Long.MIN_VALUE;
    }

    private void ensureBuff(FleetMemberAPI member) {
        BuffManagerAPI manager = member.getBuffManager();
        if (!(manager.getBuff(BUFF_ID) instanceof ConsumptionControlBuff)) {
            manager.removeBuff(BUFF_ID);
            manager.addBuff(new ConsumptionControlBuff());
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

    private Set<FleetMemberAPI> lastPlayerFleetMembers() {
        if (lastPlayerFleetMembers == null) {
            lastPlayerFleetMembers = identitySet();
        }
        return lastPlayerFleetMembers;
    }

    private static Set<FleetMemberAPI> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<FleetMemberAPI, Boolean>());
    }

    public static final class ConsumptionControlBuff implements BuffManagerAPI.Buff {
        @Override
        public void apply(FleetMemberAPI member) {
            applyMultiplier(member.getStats().getSuppliesPerMonth(),
                    ConsumptionControlSettings.getMaintenanceMultiplier());
            applyMultiplier(member.getStats().getSuppliesToRecover(),
                    ConsumptionControlSettings.getRecoverySupplyMultiplier(
                            isDockedAtMarket(Global.getSector().getPlayerFleet())));
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
