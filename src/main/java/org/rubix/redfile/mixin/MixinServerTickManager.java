package org.rubix.redfile.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.util.TimeHelper;
import net.minecraft.world.tick.TickManager;
import org.rubix.redfile.IMixinServerTickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static java.lang.Double.valueOf;

@Mixin(ServerTickManager.class)
public abstract class MixinServerTickManager extends TickManager implements IMixinServerTickManager {
    @Shadow
    private long scheduledSprintTicks;
    @Shadow
    private long sprintTicks;
    @Shadow
    private long sprintTime;
    @Shadow
    private boolean wasFrozen;
    @Final
    @Shadow
    private MinecraftServer server;

    public boolean redfile$startSprint(long ticks) {
		boolean bl = this.sprintTicks > 0L;
		this.sprintTime = 0L;
		this.scheduledSprintTicks = ticks;
		this.sprintTicks = ticks;
		this.wasFrozen = this.isFrozen();
		this.setFrozen(false);
		return bl;
	}

    public SprintResult redfile$silentFinishSprinting() {
        long l = this.scheduledSprintTicks - this.sprintTicks;
        double d = Math.max(1.0, this.sprintTime) / TimeHelper.MILLI_IN_NANOS;
        this.scheduledSprintTicks = 0L;
        this.sprintTime = 0L;
        this.sprintTicks = 0L;
        this.setFrozen(this.wasFrozen);
        this.server.updateAutosaveTicks();
        return new SprintResult(l, d);
    }
}
