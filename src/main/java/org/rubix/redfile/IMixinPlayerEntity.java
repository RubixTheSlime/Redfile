package org.rubix.redfile;

import org.rubix.redfile.results.ProfileResultTracker;

import java.util.UUID;

public interface IMixinPlayerEntity {
    ProfileResultTracker.LagStats redfile$getCumulativeLagStats(UUID trackerUuid, UUID displayId, ProfileResultTracker.LagStats newStats);
    void redfile$setCumulativeLagStats(UUID trackerUuid, UUID displayId, ProfileResultTracker.LagStats newStats);
}
