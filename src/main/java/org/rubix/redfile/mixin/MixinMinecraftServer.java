package org.rubix.redfile.mixin;

import net.minecraft.server.MinecraftServer;
import org.rubix.redfile.IMixinMinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.locks.ReentrantLock;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IMixinMinecraftServer {
    @Unique
    private static final ReentrantLock PROFILER_LOCK = new ReentrantLock();
    @Unique
    private long trialNumber = 0;

    @Override
    public ReentrantLock redfile$getProfilerLock() {
        return PROFILER_LOCK;
    }

    @Override
    public long redfile$checkoutTrialNumber() {
        ++trialNumber;
        return trialNumber;
    }
}
