package org.rubix.redfile;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import org.rubix.redfile.profiler.RedstoneProfiler;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommand {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(RedfileCommand::register);
    }


    public static class RedfileCommand {
        private static final class TrialFlags {
            public RedstoneProfiler.DurationUnit unit = RedstoneProfiler.DurationUnit.TICKS;
            public boolean isDetailed = false;
            public boolean doSprint = true;
            public boolean doLoad = true;
            public int timeScale = 1;

            private TrialFlags(TrialFlags flags) {
                this.isDetailed = flags.isDetailed;
                this.unit = flags.unit;
                this.doSprint = flags.doSprint;
                this.doLoad = flags.doLoad;
                this.timeScale = flags.timeScale;
            }

            public TrialFlags() {
            }

            private TrialFlags withDoSprint(boolean doSprint) {
                var res = new TrialFlags(this);
                res.doSprint = doSprint;
                return res;
            }

            private TrialFlags withDoLoad(boolean doLoad) {
                var res = new TrialFlags(this);
                res.doLoad = doLoad;
                return res;
            }

            private TrialFlags withIsDetailed(boolean isDetailed) {
                var res = new TrialFlags(this);
                res.isDetailed = isDetailed;
                return res;
            }

            private TrialFlags withUnit(RedstoneProfiler.DurationUnit unit) {
                var res = new TrialFlags(this);
                res.unit = unit;
                return res;
            }

            private TrialFlags withTimeScale(int timeScale) {
                var res = new TrialFlags(this);
                res.timeScale = timeScale;
                return res;
            }
        }

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
            TrialFlags flags = new TrialFlags();
            dispatcher.register(literal("redfile")
                .executes(context -> {
                    net.minecraft.text.MutableText message =
                        ((IMixinMinecraftServer)context.getSource().getWorld().getServer()).redfile$getProfilerLock().isLocked()
                            ? Text.literal("profiler currently running")
                            : Text.literal("profiler idle");
                    context.getSource().sendFeedback(() -> message, false);
                    return 1;
                })
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("summary").then(runTrial(flags.withIsDetailed(false))))
                .then(literal("detailed").then(runTrial(flags.withIsDetailed(true))))
                .then(literal("stop")
                    .executes(context -> {
                        boolean success = ((RedstoneProfilerAccess) context.getSource().getWorld()).redfile$getRedstoneProfiler().stop();
                        if (success) return 1;
                        context.getSource().sendFeedback(() -> Text.literal("profiler not running"), false);
                        return 0;
                    })
                )
                .then(literal("clear")
                    .executes(context -> {
                        ((IMixinServerWorld)context.getSource().getWorld()).redfile$clearProfileResultTrackers();
                        return 1;
                    })
                )
                .then(literal("untrim")
                    .executes(context -> {
                        ((IMixinServerWorld)context.getSource().getWorld()).redfile$untrimProfileResultTrackers();
                        return 1;
                    })
                )
            );
        }

        private static ArgumentBuilder<ServerCommandSource, ?> runTrial(TrialFlags flags) {
            return argument("from", BlockPosArgumentType.blockPos())
                .then(runTrial(argument("to", BlockPosArgumentType.blockPos()), flags)
                    .then(runTrial(literal("nolimit"), flags.withUnit(RedstoneProfiler.DurationUnit.INDEFINITE)))
                    .then(runTrial(argument("length", FloatArgumentType.floatArg(0)).suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"1000"}, builder)), flags)
                        .then(runTrial(literal("ticks"), flags))
                        .then(runTrial(literal("kticks"), flags.withTimeScale(1_000)))
                        .then(runTrial(literal("Mticks"), flags.withTimeScale(1_000_000)))
                        .then(runTrial(literal("seconds"), flags.withUnit(RedstoneProfiler.DurationUnit.SECONDS)))
                        .then(runTrial(literal("minutes"), flags.withUnit(RedstoneProfiler.DurationUnit.SECONDS).withTimeScale(60)))
                        .then(runTrial(literal("hours"), flags.withUnit(RedstoneProfiler.DurationUnit.SECONDS).withTimeScale(3600)))
                    )
                );
        }

        private static ArgumentBuilder<ServerCommandSource, ?> runTrial(
            ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
            TrialFlags flags
        ) {
            if (flags.doLoad) {
                argumentBuilder = argumentBuilder.then(runTrial(literal("noload"), flags.withDoLoad(false)));
            }

            // not currently supported
//            if (flags.doSprint) {
//                argumentBuilder = argumentBuilder.then(runTrial(literal("nosprint"), flags.withDoSprint(false)));
//            }

            return argumentBuilder.executes(context -> {
                float flength = 1000;
                try {
                    flength = FloatArgumentType.getFloat(context, "length");
                } catch (IllegalArgumentException ignored) {}
                int length = (int)(flength * flags.timeScale);

                boolean success = ((RedstoneProfilerAccess) context.getSource().getWorld()).redfile$getRedstoneProfiler().start(
                    BlockBox.create(BlockPosArgumentType.getBlockPos(context, "from"), BlockPosArgumentType.getBlockPos(context, "to")),
                    length,
                    flags.unit,
                    context.getSource(),
                    flags.isDetailed
                );
                if (success) return 1;
                context.getSource().sendFeedback(() -> Text.literal("profiler already running"), false);
                return 0;
            });
        }

    }
}
