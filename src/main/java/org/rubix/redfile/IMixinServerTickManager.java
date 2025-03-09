package org.rubix.redfile;

public interface IMixinServerTickManager {
    record SprintResult(long ticks, double sprintTime) {}

    boolean redfile$startSprint(long ticks);
    SprintResult redfile$silentFinishSprinting();

}
