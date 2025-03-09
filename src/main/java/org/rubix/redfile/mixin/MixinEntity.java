package org.rubix.redfile.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.rubix.redfile.IMixinEntity;
import org.rubix.redfile.RedstoneProfilerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements IMixinEntity {
    @Shadow public abstract World getWorld();

    @Unique
    private long sampleCount = 0;
    @Unique
    private long trialNumber = 0;

    @Override
    public void redfile$setSampleCount(long trialNumber, long amount) {
        this.trialNumber = trialNumber;
        sampleCount += amount;
    }

    @Override
    public void redfile$incSampleCount(long trialNumber) {
        if (trialNumber > this.trialNumber) {
            this.trialNumber = trialNumber;
            sampleCount = 0;
        }
        ++sampleCount;
    }

    @Override
    public long redfile$getSamples(long trialNumber) {
        if (trialNumber > this.trialNumber) {
            this.trialNumber = trialNumber;
            sampleCount = 0;
        }
        return sampleCount;
    }

    @Inject(method = "tickBlockCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V")
    )
    public void blockStepOnEnter(Vec3d lastRenderPos, Vec3d pos, CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) this.getWorld()).redfile$getRedstoneProfiler().enter(pos);
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "tickBlockCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V")
    )
    public void blockStepOnExit(Vec3d lastRenderPos, Vec3d pos, CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) this.getWorld()).redfile$getRedstoneProfiler().enter(this);
        } catch (ClassCastException ignored) {}
    }

}
