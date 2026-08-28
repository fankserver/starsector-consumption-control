package com.fs.starfarer.api;
public interface EveryFrameScript {
    boolean isDone();
    boolean runWhilePaused();
    void advance(float amount);
}
