package org.rubix.redfile.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.rubix.redfile.RedstoneProfilerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {

    @Inject(method = "neighborUpdate", at = @At("HEAD"))
    public void neighborUpdateEnter(World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
        ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(pos);
    }

    // neighbor update exit handled implicitly by other things exiting

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    public void stateChangeUpdateEnter(WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(pos);
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V", at = @At("TAIL"))
    public void stateChangeUpdateExit(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(pos);
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    public void randomTickEnter(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(pos);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    public void randomTickExit(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().exit();
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"))
    public void entityCollisionEnter(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(pos);
        } catch (ClassCastException ignored) {}
    }

    @Inject(method = "onEntityCollision", at = @At("TAIL"))
    public void entityCollisionExit(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().enter(entity);
        } catch (ClassCastException ignored) {}
    }

    // handled by packets, not ideal but works for now i guess
    @Inject(method = "onUse", at = @At("HEAD"))
    public void onUseExit(World world, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().exit();
        } catch (ClassCastException ignored) {}
    }

    // same as above
    @Inject(method = "onUseWithItem", at = @At("HEAD"))
    public void onUseWithItemExit(ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        try {
            ((RedstoneProfilerAccess) world).redfile$getRedstoneProfiler().exit();
        } catch (ClassCastException ignored) {}
    }
}
