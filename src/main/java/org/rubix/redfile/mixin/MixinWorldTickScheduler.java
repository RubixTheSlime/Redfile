package org.rubix.redfile.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.rubix.redfile.profiler.RedstoneProfiler;
import org.rubix.redfile.RedstoneProfilerAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public class MixinWorldTickScheduler<T> implements RedstoneProfilerAccess {
    @Shadow @Final private Queue<OrderedTick<T>> tickableTicks;
    @Unique
    private RedstoneProfiler REDSTONE_PROFILER;

    @Inject(method = "tick(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void tickBlockFluidExit(BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {
        REDSTONE_PROFILER.exit();
    }

    @Override
    public RedstoneProfiler redfile$getRedstoneProfiler() {
        return REDSTONE_PROFILER;
    }

    @Override
    public void redfile$setRedstoneProfiler(RedstoneProfiler profiler) {
        REDSTONE_PROFILER = profiler;
    }
}
