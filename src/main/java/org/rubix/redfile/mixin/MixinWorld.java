package org.rubix.redfile.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.jetbrains.annotations.Nullable;
import org.rubix.redfile.RedstoneProfilerAccess;
import org.rubix.redfile.block.RedfilePrimerBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow public abstract boolean isClient();

    @Shadow @Final private List<BlockEntityTickInvoker> pendingBlockEntityTickers;

    @Shadow @Final protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos);

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void tickBlockEntityEnter(CallbackInfo ci, @Local BlockEntityTickInvoker blockEntityTickInvoker) {
        try {
            ((RedstoneProfilerAccess) this).redfile$getRedstoneProfiler().enter(blockEntityTickInvoker.getPos());
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    public void tickBlockEntitiesExit(CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) this).redfile$getRedstoneProfiler().exit();
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    public void tickBlockEntitiesPrioritizePrimers(CallbackInfo ci) {
        // absolutely not the best way but whatever it's not redstone profiled
        this.blockEntityTickers.sort((a, b) ->
            (this.getBlockEntity(b.getPos()) instanceof RedfilePrimerBlockEntity ? 1 : 0) -
            (this.getBlockEntity(a.getPos()) instanceof RedfilePrimerBlockEntity ? 1 : 0)
        );
    }

}
