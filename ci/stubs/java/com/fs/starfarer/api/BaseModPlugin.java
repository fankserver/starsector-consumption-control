package com.fs.starfarer.api;
public class BaseModPlugin {
    public void onApplicationLoad() throws Exception { }
    public void onGameLoad(boolean newGame) { }
    public void beforeGameSave() { }
    public void afterGameSave() { }
    public void onGameSaveFailed() { }
    public void onEnabled(boolean enabled) { }
}
