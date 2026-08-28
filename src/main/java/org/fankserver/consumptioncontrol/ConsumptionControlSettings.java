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

    private static void reload() {
        enabled = getBoolean("cc_enabled", true);
        hyperspaceFuelMultiplier = getMultiplier("cc_hyperspaceFuelMultiplier");
        maintenanceMultiplier = getMultiplier("cc_maintenanceMultiplier");
        recoverySupplyMultiplier = getMultiplier("cc_recoverySupplyMultiplier");
        repairSpeedMultiplier = getMultiplier("cc_repairSpeedMultiplier");
        crRecoverySpeedMultiplier = getMultiplier("cc_crRecoverySpeedMultiplier");
        revision++;
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

    private ConsumptionControlSettings() { }
}
