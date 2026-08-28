package org.fankserver.consumptioncontrol;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public final class ConsumptionControlModPlugin extends BaseModPlugin {
    private ConsumptionControlScript script;

    @Override
    public void onApplicationLoad() {
        ConsumptionControlSettings.initialize();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().removeScriptsOfClass(ConsumptionControlScript.class);
        script = new ConsumptionControlScript();
        Global.getSector().addScript(script);
        script.advance(0f);
    }

    @Override
    public void beforeGameSave() {
        clearModifiers();
    }

    @Override
    public void afterGameSave() {
        if (script != null) {
            script.advance(0f);
        }
    }

    @Override
    public void onGameSaveFailed() {
        if (script != null) {
            script.advance(0f);
        }
    }

    @Override
    public void onEnabled(boolean enabled) {
        if (!enabled) {
            clearModifiers();
        }
    }

    private void clearModifiers() {
        if (script != null) {
            script.clearAll();
        }
    }
}
