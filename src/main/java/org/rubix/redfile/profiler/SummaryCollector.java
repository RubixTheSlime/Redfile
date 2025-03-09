package org.rubix.redfile.profiler;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static org.rubix.redfile.profiler.RedstoneProfiler.formatTime;

public class SummaryCollector extends DataCollector {
    private long count;


    @Override
    public void start(ServerWorld world) {
    }

    @Override
    public void incBlock(BlockPos pos) {
        ++count;
    }

    @Override
    public void incEntity(Entity entity) {
        ++count;
    }

    @Override
    public void finish(double sampleScale, ServerCommandSource source, ServerWorld world) {
        double averageLag = ((double) count) * sampleScale;
        String display = formatTime(averageLag);
        source.sendFeedback(() -> Text.literal(String.format("Average lag per tick: %s", display)), false);
    }

    @Override
    public void transfer(BlockPos from, BlockPos to) {
    }

    @Override
    public void transfer(BlockPos from, Entity to) {
    }

    @Override
    public void transfer(Entity from, BlockPos to) {
    }

    @Override
    public void transfer(Entity from, Entity to) {
    }
}
