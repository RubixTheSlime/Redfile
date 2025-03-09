package org.rubix.redfile;

import org.rubix.redfile.profiler.RedstoneProfiler;

public interface RedstoneProfilerAccess {
    RedstoneProfiler redfile$getRedstoneProfiler();

    default void redfile$setRedstoneProfiler(RedstoneProfiler profiler) {}
}
