package org.rubix.redfile.profiler;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.rubix.redfile.IMixinEntity;
import org.rubix.redfile.IMixinMinecraftServer;
import org.rubix.redfile.IMixinServerWorld;
import org.rubix.redfile.results.ProfileResultTracker;
import org.rubix.redfile.results.ResultFilter;

import java.util.Objects;
import java.util.UUID;

public class DetailedCollector extends DataCollector {
    private static final Entry defaultEntry = new Entry(0, 0, 0);
    private final Long2ObjectOpenHashMap<Entry> hashMap = new Long2ObjectOpenHashMap<>();
    private long trialNumber;

    private Entry getEntry(BlockPos pos) {
        return hashMap.computeIfAbsent(pos.asLong(), x -> new Entry(0, 0, 0));
    }

    @Override
    public void start(ServerWorld world) {
        this.trialNumber = ((IMixinMinecraftServer) world.getServer()).redfile$checkoutTrialNumber();
    }

    @Override
    public void incBlock(BlockPos pos) {
        var entry = getEntry(pos);
        ++entry.blockInstance;
        ++entry.blockPos;
    }

    @Override
    public void incEntity(Entity entity) {
        ++getEntry(entity.getBlockPos()).entityPos;
        ((IMixinEntity) entity).redfile$incSampleCount(trialNumber);
    }

    @Override
    public void finish(double sampleScale, ServerCommandSource source, ServerWorld world) {
        ProfileResultTracker tracker = new ProfileResultTracker(world, UUID.randomUUID(), (float) sampleScale);
        hashMap.forEach((blockPosLong, entry) -> {
            BlockPos pos = BlockPos.fromLong(blockPosLong);
            tracker.addBlockToArchive(world.getBlockState(pos), pos, entry.blockInstance, entry.blockPos, entry.entityPos);
        });


        tracker.setFilter(new ResultFilter());
        ((IMixinServerWorld) world).redfile$addProfileResultTracker(tracker);
    }

    @Override
    public void transfer(BlockPos from, BlockPos to) {
        if (to != null) {
            getEntry(to).blockInstance = from == null ? 0 : hashMap.getOrDefault(from.asLong(), defaultEntry).blockInstance;
        }
        if (from != null) {
            var entry = hashMap.get(from.asLong());
            if (entry != null) {
                entry.blockInstance = 0;
            }
        }

    }

    @Override
    public void transfer(BlockPos from, Entity to) {
        if (to != null) {
            ((IMixinEntity) to).redfile$setSampleCount(trialNumber,
                from == null ? 0 : hashMap.getOrDefault(from.asLong(), defaultEntry).blockInstance
            );
        }
        if (from != null) {
            var entry = hashMap.get(from.asLong());
            if (entry != null) {
                entry.blockInstance = 0;
            }
        }
    }

    @Override
    public void transfer(Entity from, BlockPos to) {
        if (to != null) {
            getEntry(to).blockInstance = from == null ? 0 : ((IMixinEntity) from).redfile$getSamples(trialNumber);
        }
        if (from != null) {
            ((IMixinEntity) from).redfile$setSampleCount(trialNumber, 0);
        }

    }

    @Override
    public void transfer(Entity from, Entity to) {
        if (to != null) {
            ((IMixinEntity) to).redfile$setSampleCount(trialNumber,
                from == null ? 0 : ((IMixinEntity) from).redfile$getSamples(trialNumber)
            );
        }
        if (from != null) {
            ((IMixinEntity) from).redfile$setSampleCount(trialNumber, 0);
        }
    }

    private static final class Entry {
        private long blockInstance;
        private long blockPos;
        private long entityPos;

        private Entry(long blockInstance, long blockPos, long entityPos) {
            this.blockInstance = blockInstance;
            this.blockPos = blockPos;
            this.entityPos = entityPos;
        }

        public long blockInstance() {
            return blockInstance;
        }

        public long blockPos() {
            return blockPos;
        }

        public long entityPos() {
            return entityPos;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Entry) obj;
            return this.blockInstance == that.blockInstance &&
                this.blockPos == that.blockPos &&
                this.entityPos == that.entityPos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockInstance, blockPos, entityPos);
        }

        @Override
        public String toString() {
            return "Entry[" +
                "blockInstance=" + blockInstance + ", " +
                "blockPos=" + blockPos + ", " +
                "entityPos=" + entityPos + ']';
        }
    }
}
