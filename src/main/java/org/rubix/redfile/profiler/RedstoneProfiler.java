package org.rubix.redfile.profiler;

import it.unimi.dsi.fastutil.Function;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.rubix.redfile.IMixinMinecraftServer;
import org.rubix.redfile.IMixinServerTickManager;

import java.util.Random;
import java.util.function.Supplier;

public class RedstoneProfiler {
    private final ServerWorld serverWorld;
    private ServerCommandSource commandSource;
    private ProfileEnder profileEnder;
    private Sampler sampler = new Sampler();
    private DataCollector collector;
    private TrialFilter filter;
    private boolean running;
    private final Random random = new Random();

    public RedstoneProfiler(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }

    public enum DurationUnit {
        TICKS,
        SECONDS,
        INDEFINITE,
    }

    public boolean start(BlockBox box, int length, DurationUnit unit, ServerCommandSource source, boolean detailed) {
        if (running) return false;
        if (!((IMixinMinecraftServer) serverWorld.getServer()).redfile$getProfilerLock().tryLock()) return false;

        running = true;
        commandSource = source;
        profileEnder = switch (unit) {
            case TICKS -> new ProfileEnder.TickProfileEnder(length);
            case SECONDS -> new ProfileEnder.TimeProfileEnder(length);
            case INDEFINITE -> new ProfileEnder.IndefiniteProfileEnder();
        };
        collector = detailed ? new DetailedCollector() : new SummaryCollector();
        collector.start(serverWorld);
        filter = new TrialFilter.BoxTrialFilter(box);
        sampler = new Sampler(collector, filter);
        sampler.start();
        ((IMixinServerTickManager) serverWorld.getTickManager()).redfile$startSprint(Long.MAX_VALUE);

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }
        profileEnder.stop();
        sampler.stop();
        running = false;

        var tickSprintResults = ((IMixinServerTickManager) serverWorld.getTickManager()).redfile$silentFinishSprinting();
        double sampleScale = tickSprintResults.sprintTime() / (double) (sampler.getTotalSamples() * tickSprintResults.ticks());
//        commandSource.sendFeedback(() -> Text.literal(String.format("number of samples: %d", sampler.getTotalSamples())), false);
        collector.finish(sampleScale, commandSource, serverWorld);

        profileEnder = null;
        collector = null;
        filter = null;
        sampler = new Sampler();

        ((IMixinMinecraftServer) serverWorld.getServer()).redfile$getProfilerLock().unlock();
        return true;
    }

    public void tick() {
        if (!running) return;
        if (profileEnder.tick()) stop();
    }

//    public boolean isRunning() {
//        return running;
//    }
    
    public void enter(Object pos) {
        this.sampler.setItem(pos);
    }

    public void exit() {
        sampler.setItem(null);
        // long samples = sampler.getSamples();
        // if (samples == 0) return;
        // if (filter.test(pos)) collector.incBlock(pos, samples);
        // this.sampler.resetSamples();
    }

    // public record ExitBlocksRecord(int count, Function<Integer, BlockPos> getter) {}

    // public void exitMultiple(Supplier<ExitBlocksRecord> supplier) {
    //     long samples = sampler.getSamples();
    //     if (samples == 0) return;

    //     var things = supplier.get();
    //     int count = things.count;
    //     if (count == 0) return;
    //     var getter = things.getter;

    //     for(int i = 0; i < samples; ++i) {
    //         int index = random.nextInt(count);
    //         var pos = getter.apply(index);
    //         if (filter.test(pos)) collector.incBlock(pos, 1);
    //     }

    //     this.sampler.resetSamples();
    // }

    // public void exit(Entity entity) {
    //     long samples = sampler.getSamples();
    //     if (samples == 0) return;
    //     if (filter.test(entity)) collector.incEntity(entity, samples);
    //     this.sampler.resetSamples();
    // }

    public void transfer(BlockPos from, BlockPos to) {
        collector.getLock().lock();
        try {
            collector.transfer(filter.test(from) ? from : null, filter.test(from) ? to : null);
        } finally {
            collector.getLock().unlock();
        }
    }

    public void transfer(BlockPos from, Entity to) {
        collector.getLock().lock();
        try {
            collector.transfer(filter.test(from) ? from : null, filter.test(from) ? to : null);
        } finally {
            collector.getLock().unlock();
        }
    }

    public void transfer(Entity from, BlockPos to) {
        collector.getLock().lock();
        try {
            collector.transfer(filter.test(from) ? from : null, filter.test(from) ? to : null);
        } finally {
            collector.getLock().unlock();
        }
    }

    public void transfer(Entity from, Entity to) {
        collector.getLock().lock();
        try {
            collector.transfer(filter.test(from) ? from : null, filter.test(from) ? to : null);
        } finally {
            collector.getLock().unlock();
        }
    }

    public static @NotNull String formatTime(double millis) {
        double scaled = millis;
        String unit = "ms";
        if (millis >= 3_600_000d) {
            scaled = millis / 3_600_000d;
            unit = "hr";
        } else if (millis >= 60_000d) {
            scaled = millis / 60_000d;
            unit = "min";
        } else if (millis >= 1_000d) {
            scaled = millis / 1_000d;
            unit = "s";
        } else if (millis < 0.000_001d) {
            return "<1ns";
        } else if (millis < 0.001d) {
            scaled = millis * 1_000_000d;
            unit = "ns";
        } else if (millis < 1d) {
            scaled = millis * 1_000d;
            unit = "μs";
        }
        String accuracy = "%.1f%s";
        if (scaled < 10d) accuracy = "%.3f%s";
        else if (scaled < 100d) accuracy = "%.2f%s";
        return String.format(accuracy, scaled, unit);
    }

}
