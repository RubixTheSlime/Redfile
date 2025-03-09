package org.rubix.redfile.results;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class BlockResultData {
    private final BlockState state;
    private final long blockInstance;
    private final long blockPosition;
    private final long entityPosition;
    public long value = 0;
    public int entityId = -1;
    public byte connections = 0;

    BlockResultData(BlockState state, long blockInstance, long blockPosition, long entityPosition) {
        this.state = state;
        this.blockInstance = blockInstance;
        this.blockPosition = blockPosition;
        this.entityPosition = entityPosition;
    }

    public BlockState state() {
        return state;
    }

    public long blockInstance() {
        return blockInstance;
    }

    public long blockPosition() {
        return blockPosition;
    }

    public long entityPosition() {
        return entityPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockResultData) obj;
        return Objects.equals(this.state, that.state) &&
            this.blockInstance == that.blockInstance &&
            this.blockPosition == that.blockPosition &&
            this.entityPosition == that.entityPosition;
    }

    public final boolean getConnection(Direction direction) {
        return (connections & (byte) (1 << direction.getId())) != 0;
    }

    public final void setConnection(Direction direction, boolean value) {
        byte bit = (byte) (1 << direction.getId());
        connections = ((byte) (connections & (~bit) | (value ? bit : 0)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, blockInstance, blockPosition, entityPosition);
    }

    @Override
    public String toString() {
        return "ArchiveBlockData[" +
            "state=" + state + ", " +
            "blockInstance=" + blockInstance + ", " +
            "blockPosition=" + blockPosition + ", " +
            "entityPosition=" + entityPosition + ']';
    }

}
