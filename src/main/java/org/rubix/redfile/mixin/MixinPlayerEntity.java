package org.rubix.redfile.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.rubix.redfile.IMixinPlayerEntity;
import org.rubix.redfile.results.ProfileResultTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements IMixinPlayerEntity {
    @Unique
    private UUID mostRecentTracker = null;
    @Unique
    private ProfileResultTracker.LagStats cumulativeStats = new ProfileResultTracker.LagStats(0, 0, 0);
    @Unique
    private final HashSet<UUID> alreadyAdded = new HashSet<>();

    @Override
    public ProfileResultTracker.LagStats redfile$getCumulativeLagStats(UUID trackerUuid, UUID displayId, ProfileResultTracker.LagStats newStats) {
        if (trackerUuid != mostRecentTracker) {
            cumulativeStats = newStats;
            alreadyAdded.clear();
            mostRecentTracker = trackerUuid;
        } else if (!alreadyAdded.contains(displayId)) {
            cumulativeStats.add(newStats);
        }
        alreadyAdded.add(displayId);
        return cumulativeStats;
    }

    @Override
    public void redfile$setCumulativeLagStats(UUID trackerUuid, UUID displayId, ProfileResultTracker.LagStats newStats) {
        mostRecentTracker = trackerUuid;
        alreadyAdded.clear();
        cumulativeStats = newStats;
        alreadyAdded.add(displayId);
    }
}
