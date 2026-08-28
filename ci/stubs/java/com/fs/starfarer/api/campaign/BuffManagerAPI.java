package com.fs.starfarer.api.campaign;
public interface BuffManagerAPI {
    Buff getBuff(String id);
    void addBuff(Buff buff);
    void removeBuff(String id);

    interface Buff {
        void apply(com.fs.starfarer.api.fleet.FleetMemberAPI member);
        String getId();
        boolean isExpired();
        void advance(float amount);
    }
}
