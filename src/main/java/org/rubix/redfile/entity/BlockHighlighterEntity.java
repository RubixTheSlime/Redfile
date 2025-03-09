package org.rubix.redfile.entity;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.rubix.redfile.IMixinPlayerEntity;
import org.rubix.redfile.IMixinServerWorld;
import org.rubix.redfile.results.ProfileResultTracker;

import java.util.Set;
import java.util.UUID;

public class BlockHighlighterEntity extends Entity {
//	public static byte CONNECTION_MASK_UP = (byte) (1 << Direction.UP.getId());
//	public static byte CONNECTION_MASK_DOWN = (byte) (1 << Direction.DOWN.getId());
//	public static byte CONNECTION_MASK_EAST = (byte) (1 << Direction.EAST.getId());
//	public static byte CONNECTION_MASK_WEST = (byte) (1 << Direction.WEST.getId());
//	public static byte CONNECTION_MASK_NORTH = (byte) (1 << Direction.NORTH.getId());
//	public static byte CONNECTION_MASK_SOUTH = (byte) (1 << Direction.SOUTH.getId());

//	public static final String BLOCK_STATE_NBT_KEY = "block";
	public static final String COLOR_NBT_KEY = "color";
	public static final String CONNECTIONS_NBT_KEY = "connections";
	public static final String TRACKER_NBT_KEY = "tracker";
	public static final String ENABLED_NBT_KEY = "enabled";
//	private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(
//		BlockHighlighterEntity.class, TrackedDataHandlerRegistry.BLOCK_STATE
//	);
	private static final TrackedData<Integer> COLOR = DataTracker.registerData(
		BlockHighlighterEntity.class, TrackedDataHandlerRegistry.INTEGER
	);
	private static final TrackedData<Byte> CONNECTIONS = DataTracker.registerData(
		BlockHighlighterEntity.class, TrackedDataHandlerRegistry.BYTE
	);
	private static final TrackedData<Boolean> ENABLED = DataTracker.registerData(
		BlockHighlighterEntity.class, TrackedDataHandlerRegistry.BOOLEAN
	);
	public UUID trackerUUID = null;

	public BlockHighlighterEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
		this.noClip = true;
	}

    @Override
    public void tick() {
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
//		builder.add(BLOCK_STATE, Blocks.AIR.getDefaultState());
		builder.add(COLOR, 0);
		builder.add(CONNECTIONS, (byte) 0);
		builder.add(ENABLED, false);
    }

	@Override
	public boolean canHit() {
		return getEnabled();
	}

	@Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
		var attacker = source.getAttacker();
		if (attacker instanceof PlayerEntity) this.getResultTracker().setMinimum(attacker.isSneaking() ? null : this);
        return false;
    }

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		return displayStats(player, !player.isSneaking());
	}

	private ActionResult displayStats(PlayerEntity player, boolean cumulative) {
		var tracker = getResultTracker();
		var pos = this.getBlockPos().up();
		var stats = tracker == null ? null : tracker.getBlockStats(pos);
		if (stats == null || stats.millis() == 0) return ActionResult.PASS;

        if (!cumulative) {
            ((IMixinPlayerEntity) player).redfile$setCumulativeLagStats(trackerUUID, getUuid(), stats);
        }
        var cumStats = ((IMixinPlayerEntity) player).redfile$getCumulativeLagStats(trackerUUID, getUuid(), stats);
		player.sendMessage(Text.of(String.format("%s   sum: %s", stats, cumStats)), true);
		return ActionResult.SUCCESS;
	}

	private ProfileResultTracker getResultTracker() {
		if (trackerUUID == null || this.getWorld().isClient) return null;
		return ((IMixinServerWorld) this.getWorld()).redfile$getProfileResultTracker(trackerUUID);
	}

	private void trackerRegister(boolean register) {
		if (this.getWorld().isClient) return;
		var tracker = getResultTracker();
		var pos = this.getBlockPos().up();
		var entry = tracker == null ? null : tracker.getBlockData(pos);
		if (register) {
			if (entry == null || entry.entityId != -1) {
				((IMixinServerWorld) this.getWorld()).redfile$markForEventualRemoval(this);
				return;
			}
            tracker.setEntity(pos, this);
		} else {
			if (entry == null || entry.entityId != getId()) return;
			tracker.setEntity(pos, null);
		}
		tracker.markDirty(pos);
	}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
		trackerRegister(false);
//        this.dataTracker.set(BLOCK_STATE, NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound(BLOCK_STATE_NBT_KEY)));
		if (nbt.contains(COLOR_NBT_KEY)) {
			this.setColor(nbt.getInt(COLOR_NBT_KEY));
		}
		if (nbt.contains(CONNECTIONS_NBT_KEY)) {
			this.setConnections(nbt.getByte(CONNECTIONS_NBT_KEY));
		}
		if (nbt.contains(TRACKER_NBT_KEY)) {
			this.trackerUUID = nbt.getUuid(TRACKER_NBT_KEY);
		}
		if (nbt.contains(ENABLED_NBT_KEY)) {
			this.setEnabled(nbt.getBoolean(ENABLED_NBT_KEY));
		}
		trackerRegister(true);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
//		nbt.put(BLOCK_STATE_NBT_KEY, NbtHelper.fromBlockState(this.dataTracker.get(BLOCK_STATE)));
		nbt.putInt(COLOR_NBT_KEY, this.getColor());
		nbt.putByte(CONNECTIONS_NBT_KEY, this.getConnections());
		nbt.putBoolean(ENABLED_NBT_KEY, this.getEnabled());
		nbt.putUuid(TRACKER_NBT_KEY, this.trackerUUID);
    }

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		trackerRegister(false);
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return false;
	}

	@Override
	protected boolean couldAcceptPassenger() {
		return false;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
	}

	@Override
	public PistonBehavior getPistonBehavior() {
		return PistonBehavior.IGNORE;
	}

	@Override
	public boolean canAvoidTraps() {
        return true;
    }

	@Override
	public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera) {
		return false;
	}

	@Override
	public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
	}

	public final void setColor(int color) {
		this.dataTracker.set(COLOR, color);
	}

	public final int getColor() {
		return this.dataTracker.get(COLOR);
	}

	public final void setConnections(byte connections) {
		this.dataTracker.set(CONNECTIONS, connections);
	}

	public final byte getConnections() {
		return this.dataTracker.get(CONNECTIONS);
	}

	public final void setEnabled(boolean enabled) {
		this.dataTracker.set(ENABLED, enabled);
	}

	public final boolean getEnabled() {
		return this.dataTracker.get(ENABLED);
	}

	public final void setConnection(Direction direction, boolean value) {
		byte bit = (byte) (1 << direction.getId());
		setConnections((byte) (getConnections() & (~bit) | (value ? bit : 0)));
	}

	public final boolean getConnection(Direction direction) {
		return (getConnections() & (byte) (1 << direction.getId())) != 0;
	}
}
