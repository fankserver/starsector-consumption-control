package org.fankserver.consumptioncontrol;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public final class ConsumptionControlSettings implements LunaSettingsListener {
    public static final String MOD_ID = "consumption_control";

    private static boolean enabled = true;
    private static float hyperspaceFuelMultiplier = 1f;
    private static float maintenanceMultiplier = 1f;
    private static float recoverySupplyMultiplier = 1f;
    private static float repairSpeedMultiplier = 1f;
    private static float crRecoverySpeedMultiplier = 1f;
    private static long revision = 0L;

    public static void initialize() {
        if (!LunaSettings.hasSettingsListenerOfClass(ConsumptionControlSettings.class)) {
            LunaSettings.addSettingsListener(new ConsumptionControlSettings());
        }
        reload();
    }

    @Override
    public void settingsChanged(String modId) {
        if (MOD_ID.equals(modId)) {
            reload();
        }
    }

    public static void refresh() {
        boolean newEnabled = getBoolean("cc_enabled", true);
        float newHyperspaceFuelMultiplier = getMultiplier("cc_hyperspaceFuelMultiplier");
        float newMaintenanceMultiplier = getMultiplier("cc_maintenanceMultiplier");
        float newRecoverySupplyMultiplier = getMultiplier("cc_recoverySupplyMultiplier");
        float newRepairSpeedMultiplier = getMultiplier("cc_repairSpeedMultiplier");
        float newCrRecoverySpeedMultiplier = getMultiplier("cc_crRecoverySpeedMultiplier");

        if (enabled == newEnabled
                && Float.compare(hyperspaceFuelMultiplier, newHyperspaceFuelMultiplier) == 0
                && Float.compare(maintenanceMultiplier, newMaintenanceMultiplier) == 0
                && Float.compare(recoverySupplyMultiplier, newRecoverySupplyMultiplier) == 0
                && Float.compare(repairSpeedMultiplier, newRepairSpeedMultiplier) == 0
                && Float.compare(crRecoverySpeedMultiplier, newCrRecoverySpeedMultiplier) == 0) {
            return;
        }

        enabled = newEnabled;
        hyperspaceFuelMultiplier = newHyperspaceFuelMultiplier;
        maintenanceMultiplier = newMaintenanceMultiplier;
        recoverySupplyMultiplier = newRecoverySupplyMultiplier;
        repairSpeedMultiplier = newRepairSpeedMultiplier;
        crRecoverySpeedMultiplier = newCrRecoverySpeedMultiplier;
        revision++;
    }

    private static void reload() {
        refresh();
    }

    private static boolean getBoolean(String fieldId, boolean fallback) {
        Boolean value = LunaSettings.getBoolean(MOD_ID, fieldId);
        return value == null ? fallback : value;
    }

    private static float getMultiplier(String fieldId) {
        Float value = LunaSettings.getFloat(MOD_ID, fieldId);
        if (value == null || Float.isNaN(value) || Float.isInfinite(value)) {
            return 1f;
        }
        return Math.max(0f, Math.min(2f, value));
    }

    public static boolean isEnabled() { return enabled; }
    public static float getHyperspaceFuelMultiplier() { return hyperspaceFuelMultiplier; }
    public static float getMaintenanceMultiplier() { return maintenanceMultiplier; }
    public static float getRecoverySupplyMultiplier() { return recoverySupplyMultiplier; }
    public static float getRepairSpeedMultiplier() { return repairSpeedMultiplier; }
    public static float getCrRecoverySpeedMultiplier() { return crRecoverySpeedMultiplier; }
    public static long getRevision() { return revision; }

    public static boolean hasShipAdjustments() {
        return Float.compare(maintenanceMultiplier, 1f) != 0
                || Float.compare(recoverySupplyMultiplier, 1f) != 0
                || Float.compare(repairSpeedMultiplier, 1f) != 0
                || Float.compare(crRecoverySpeedMultiplier, 1f) != 0;
    }

    private ConsumptionControlSettings() { }
}
