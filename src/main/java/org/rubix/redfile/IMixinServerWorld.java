package org.rubix.redfile;

import net.minecraft.entity.Entity;
import org.rubix.redfile.results.ProfileResultTracker;

import java.util.UUID;

public interface IMixinServerWorld {
    ProfileResultTracker redfile$getProfileResultTracker(UUID uuid);
    void redfile$addProfileResultTracker(ProfileResultTracker tracker);
    void redfile$removeProfileResultTracker(UUID uuid);
    void redfile$clearProfileResultTrackers();
    void redfile$untrimProfileResultTrackers();

    void redfile$markForEventualRemoval(Entity entity);
}
