package org.rubix.redfile.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.rubix.redfile.ModBlockEntityTypes;
import org.rubix.redfile.Redfile;

import static org.rubix.redfile.Redfile.LOGGER;

public class RedfilePrimerBlockEntity extends BlockEntity {
    public RedfilePrimerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.REDFILE_PRIMER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState blockState, RedfilePrimerBlockEntity redfilePrimerBlockEntity) {
        Redfile.noOp();
    }
}
