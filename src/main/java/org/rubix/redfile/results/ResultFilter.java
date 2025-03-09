package org.rubix.redfile.results;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class ResultFilter {

    public boolean testBlock(BlockState state, BlockPos pos) {
        return true;
    }

    public boolean testEntity(Entity entity) {
        return true;
    }

    public long getAmount(BlockResultData blockResultData, BlockPos pos) {
        return blockResultData.blockPosition() + blockResultData.entityPosition();
    }

}
