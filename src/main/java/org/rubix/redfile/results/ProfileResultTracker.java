package org.rubix.redfile.results;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.rubix.redfile.ModEntity;
import org.rubix.redfile.entity.BlockHighlighterEntity;

import java.awt.*;
import java.util.UUID;

import static org.rubix.redfile.profiler.RedstoneProfiler.formatTime;

public class ProfileResultTracker {
    private final ServerWorld world;
    private final UUID uuid;
    private final Long2ObjectOpenHashMap<ChunkEntry> chunks = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet dirtyPositions = new LongOpenHashSet();
    private boolean allDirty = false;
    private double alphaScale;
    private double alphaOffset;
    private double percentageScale;
    private final double timeScale;
    private long minimum;

    public ProfileResultTracker(ServerWorld world, UUID uuid, float timeScale) {
        this.world = world;
        this.uuid = uuid;
        this.timeScale = timeScale;
    }

    public void addBlockToArchive(BlockState state, BlockPos pos, long blockInstance, long blockPosition, long entityPosition) {
        long chunkPos = chunkPos(pos);
        var chunkEntry = chunks.computeIfAbsent(chunkPos, x -> new ChunkEntry());
        chunkEntry.blockData.put(pos.asLong(), new BlockResultData(state, blockInstance, blockPosition, entityPosition));
        ++chunkEntry.missingEntities;
    }

    public BlockResultData getBlockData(BlockPos pos) {
        var chunk = this.chunks.get(world.getWorldChunk(pos).getPos().toLong());
        if (chunk == null) return null;
        return chunk.blockData.get(pos.asLong());
    }

    public LagStats getBlockStats(BlockPos pos) {
        var data = getBlockData(pos);
        if (data == null) return new LagStats(0, 0, 0);
        return new LagStats(data.value, timeScale, percentageScale);
    }

    public void setEntity(BlockPos pos, Entity entity) {
        var chunk = this.chunks.get(world.getWorldChunk(pos).getPos().toLong());
        if (chunk == null) return;
        var entry = chunk.blockData.get(pos.asLong());
        if (entry.entityId == -1 && entity != null) --chunk.missingEntities;
        if (entry.entityId != -1 && entity == null) ++chunk.missingEntities;
        entry.entityId = entity == null ? -1 : entity.getId();
    }

    public void tick() {
        chunks.forEach((chunkPos, chunkEntry) -> {
            if (world.isChunkLoaded(chunkPos) && chunkEntry.missingEntities > 0) {
                chunkEntry.blockData.forEach((blockPosLong, blockEntry) -> {
                    if (blockEntry.entityId == -1) {
                        markDirty(blockPosLong);
                        var entity = ModEntity.BLOCK_HIGHLIGHTER_ENTITY_ENTITY_TYPE.create(world, SpawnReason.EVENT);
                        assert entity != null;
                        var blockPos = BlockPos.fromLong(blockPosLong);
                        entity.setPosition(blockPos.getX() + 0.5d, blockPos.getY() - 1d/16, blockPos.getZ() + 0.5);
                        entity.trackerUUID = uuid;
                        blockEntry.entityId = entity.getId();
                        world.spawnEntity(entity);
                    }
                });
            }
        });

        if (allDirty) {
            chunks.forEach((chunkPosLong, chunk) -> {
                if (world.isChunkLoaded(chunkPosLong))
                    chunk.blockData.forEach((blockPosLong, blockData) -> updateEntry(BlockPos.fromLong(blockPosLong), blockData));
            });
            allDirty = false;
        } else if (!dirtyPositions.isEmpty()) {
            dirtyPositions.forEach(blockPos -> updateEntry(BlockPos.fromLong(blockPos), getBlockData(BlockPos.fromLong(blockPos))));
            dirtyPositions.clear();
        }
    }

    private void updateEntry(BlockPos pos, BlockResultData data) {
        var entity = (BlockHighlighterEntity) world.getEntityById(data.entityId);
        assert entity != null;
        boolean enabled = data.value > minimum;
        entity.setEnabled(enabled);
        if (enabled) {
            double intensity = Math.min(Math.max(Math.log(data.value * timeScale) * (1d / (Math.log(10) * 6)), -5d / 6), 0);
            double hue = ((5d / 6) - intensity) % 1d;
//            double alpha = Math.max(Math.min((Math.log(data.value) + alphaOffset) * (1d / (Math.log(10) * 4)) + 1, 1), 0.4) - 0.3;
            double alpha = (intensity * (0.6 * 6d / 5)) + 0.8;

            entity.setColor(Color.HSBtoRGB((float) hue, 1, 1) + (((int) (alpha * 255)) << 24));
            entity.setConnections(data.connections);
        }
    }

    public void setFilter(ResultFilter filter) {
        var ref = new Object() {
            long sum = 0;
            long qSum = 0;
            double cSum = 0;
            double hSum = 0;
            double gSum = 0;
            long max = 0;
            long count = 0;
            boolean anythingChanged;
        };
        chunks.forEach((chunkPosLong, chunk) -> chunk.blockData.forEach((blockPosLong, blockData) ->{
            long amount = filter.getAmount(blockData, BlockPos.fromLong(blockPosLong));
            blockData.value = amount;
            ref.sum += amount;
            ref.max = Long.max(ref.max, amount);
            ref.qSum += amount * amount;
            ref.cSum += Math.pow(amount, 3);
            if (blockData.value != 0) {
                ++ref.count;
                ref.hSum += 1d / amount;
                ref.gSum += Math.log(amount);
            }
        }));

        chunks.forEach((chunkPosLong, chunk) -> chunk.blockData.forEach((blockPosLong, blockData) -> {
            BlockPos pos = BlockPos.fromLong(blockPosLong);
            for (var direction : Direction.values()) {
                var other = getBlockData(pos.offset(direction));
                boolean res;
                if (other == null || blockData.value > other.value) {
                    res = true;
                } else if (blockData.value == other.value) {
                    res = other.getConnection(direction.getOpposite());
                } else {
                    res = false;
                }
                blockData.setConnection(direction, res);
            }
        }));

        if (ref.count == 0) return;

        percentageScale = 100d / ref.sum;
        double aMean = (double) ref.sum / ref.count;
        double hMean = (double) ref.count / ref.hSum;
        double qMean = Math.sqrt((double) ref.qSum / ref.count);
        double cMean = Math.cbrt((double) ref.cSum / ref.count);
        double gMean = Math.exp(ref.gSum / ref.count);
        alphaScale = 1f / Math.log(aMean);
        alphaOffset =  -Math.log(ref.max);
        markAllDirty();
    }

    public void clear() {
        chunks.forEach((chunkPosLong, chunk) -> {
            if (world.isChunkLoaded(chunkPosLong))
                chunk.blockData.forEach((blockPosLong, blockData) -> {
                    var entity = world.getEntityById(blockData.entityId);
                    if (entity != null) entity.kill(world);
                });
        });
    }

    private long chunkPos(BlockPos pos) {
        return world.getWorldChunk(pos).getPos().toLong();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void markDirty(BlockPos pos) {
        markDirty(pos.asLong());
    }

    public void markDirty(long pos) {
        if (!allDirty) dirtyPositions.add(pos);
    }

    public void markAllDirty() {
        allDirty = true;
        dirtyPositions.clear();
    }

    public void setMinimum(BlockHighlighterEntity entity) {
        this.minimum = entity == null ? 0 : getBlockData(entity.getBlockPos().up()).value;
        markAllDirty();
//        chunks.forEach((chunkPosLong, chunk) -> chunk.blockData.forEach((blockPosLong, blockData) -> {
//            if (blockData.value <= minimum) markDirty(blockPosLong);
//        }));
    }

    private static class ChunkEntry {
        public int missingEntities = 0;
        private final Long2ObjectOpenHashMap<BlockResultData> blockData = new Long2ObjectOpenHashMap<>();

    }

    public static final class LagStats {
        private long amount;
        private final double millisScale;
        private final double percentageScale;

        public LagStats(long amount, double millisScale, double percentageScale) {
            this.amount = amount;
            this.millisScale = millisScale;
            this.percentageScale = percentageScale;
        }

        public double millis() {
            return amount * millisScale;
        }

        public double percentage() {
            return amount * percentageScale;
        }

        public void add(LagStats other) {
            amount += other.amount;
        }

        @Override
        public String toString() {
            return String.format("%s  %.03f%%", formatTime(millis()), percentage());
        }
    }
}
