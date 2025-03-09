package org.rubix.redfile;

import java.util.concurrent.locks.ReentrantLock;

public interface IMixinMinecraftServer {
    ReentrantLock redfile$getProfilerLock();
    long redfile$checkoutTrialNumber();
}
