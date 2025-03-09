package org.rubix.redfile.profiler;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class DataCollector {
    private final Lock lock = new ReentrantLock();
    
    public abstract void start(ServerWorld world);

    public abstract void incBlock(BlockPos pos);

    public abstract void incEntity(Entity entity);
    
    public void inc(Object object) {
        if (object instanceof BlockPos) incBlock((BlockPos) object);
        if (object instanceof Entity) incEntity((Entity) object);
    }
    
    public Lock getLock() {
        return lock;
    }

    public abstract void finish(double sampleScale, ServerCommandSource source, ServerWorld world);

    public abstract void transfer(BlockPos from, BlockPos to);

    public abstract void transfer(BlockPos from, Entity to);

    public abstract void transfer(Entity from, BlockPos to);

    public abstract void transfer(Entity from, Entity to);
}
