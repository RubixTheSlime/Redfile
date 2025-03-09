package org.rubix.redfile.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.rubix.redfile.IMixinServerWorld;
import org.rubix.redfile.profiler.RedstoneProfiler;
import org.rubix.redfile.RedstoneProfilerAccess;
import org.rubix.redfile.results.ProfileResultTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(value = ServerWorld.class)
public abstract class MixinServerWorld implements RedstoneProfilerAccess, IMixinServerWorld {
    @Shadow public abstract ServerWorld toServerWorld();

    @Unique
    public final RedstoneProfiler REDSTONE_PROFILER = new RedstoneProfiler(this.toServerWorld());
    @Unique
    public final Object2ObjectOpenHashMap<UUID, ProfileResultTracker> PROFILE_RESULTS_TRACKERS = new Object2ObjectOpenHashMap<>();
    @Unique
    public final IntOpenHashSet EVENTUAL_REMOVAL = new IntOpenHashSet();

    @Override
    public RedstoneProfiler redfile$getRedstoneProfiler() {
        return REDSTONE_PROFILER;
    }

    @Override
    public ProfileResultTracker redfile$getProfileResultTracker(UUID uuid) {
        return PROFILE_RESULTS_TRACKERS.get(uuid);
    }

    @Override
    public void redfile$addProfileResultTracker(ProfileResultTracker tracker) {
        PROFILE_RESULTS_TRACKERS.put(tracker.getUuid(), tracker);
    }

    @Override
    public void redfile$removeProfileResultTracker(UUID uuid) {
        PROFILE_RESULTS_TRACKERS.remove(uuid).clear();
    }

    @Override
    public void redfile$clearProfileResultTrackers() {
        PROFILE_RESULTS_TRACKERS.forEach((uuid, tracker) -> tracker.clear());
        PROFILE_RESULTS_TRACKERS.clear();
    }

    @Override
    public void redfile$untrimProfileResultTrackers() {
        PROFILE_RESULTS_TRACKERS.forEach((uuid, tracker) -> tracker.setMinimum(null));
    }

    @Override
    public void redfile$markForEventualRemoval(Entity entity) {
        EVENTUAL_REMOVAL.add(entity.getId());
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Shadow @Final private WorldTickScheduler<Block> blockTickScheduler;

    @Shadow @Final private WorldTickScheduler<Fluid> fluidTickScheduler;

    @Shadow public abstract @Nullable Entity getEntity(UUID uuid);

    @Shadow public abstract @Nullable Entity getEntityById(int id);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ((RedstoneProfilerAccess) this.blockTickScheduler).redfile$setRedstoneProfiler(REDSTONE_PROFILER);
        ((RedstoneProfilerAccess) this.fluidTickScheduler).redfile$setRedstoneProfiler(REDSTONE_PROFILER);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHead(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        PROFILE_RESULTS_TRACKERS.forEach((uuid, tracker) -> tracker.tick());
        REDSTONE_PROFILER.tick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickTail(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        EVENTUAL_REMOVAL.forEach(entityId -> {
            var entity = this.getEntityById(entityId);
            if (entity != null) entity.kill(this.toServerWorld());
        });
        EVENTUAL_REMOVAL.clear();
    }

    @Inject(method = "tickBlock", at = @At("HEAD"))
    public void tickBlockEnter(BlockPos pos, Block block, CallbackInfo ci) {
        REDSTONE_PROFILER.enter(pos);
    }

    @Inject(method = "tickFluid", at = @At("HEAD"))
    public void tickFluidEnter(BlockPos pos, Fluid fluid, CallbackInfo ci) {
        REDSTONE_PROFILER.enter(pos);
    }

    // tile tick exit handled by WorldTickScheduler
    // random ticks handled in AbstractBlockState

    @Inject(method = "processBlockEvent", at = @At("HEAD"))
    public void processBlockEventEnter(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        REDSTONE_PROFILER.enter(event.pos());
    }

    // todo: make slightly more accurate hopefully
    @Inject(method = "processSyncedBlockEvents", at = @At("TAIL"))
    public void processSyncedBlockEventsExit(CallbackInfo ci) {
        REDSTONE_PROFILER.exit();
    }

    @Inject(method = "tickEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    public void tickEntityEnter(Entity entity, CallbackInfo ci) {
        REDSTONE_PROFILER.enter(entity);
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"))
    public void tickPassengerEnter(Entity vehicle, Entity passenger, CallbackInfo ci) {
        REDSTONE_PROFILER.enter(passenger);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"))
    public void tickEntitiesExit(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        REDSTONE_PROFILER.exit();
    }

    // block entities handled in World

//    @Inject(method = "processBlockEvent(Lnet/minecraft/server/world/BlockEvent;)Z",
//        at = @At("TAIL"))
//    public void processBlockEventTail(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
//    }
//
//    @Inject(method = "",
//        at = @At("TAIL"))
//    public void Tail() {}
//
//    @Inject(method = "",
//        at = @At("TAIL"))
//    public void Tail() {}
//
//    @Inject(method = "",
//        at = @At("TAIL"))
//    public void Tail() {}
//
//    @Inject(method = "",
//    at = @At("TAIL"))
//    public void Tail() {}

}
