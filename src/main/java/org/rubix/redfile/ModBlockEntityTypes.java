package org.rubix.redfile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.rubix.redfile.block.RedfilePrimerBlockEntity;

import static org.rubix.redfile.Redfile.MOD_ID;

public class ModBlockEntityTypes {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, path), blockEntityType);
    }

    public static final BlockEntityType<RedfilePrimerBlockEntity> REDFILE_PRIMER_BLOCK_ENTITY = register(
        "redfile_primer_block",
        FabricBlockEntityTypeBuilder.create(RedfilePrimerBlockEntity::new, ModBlock.REDFILE_PRIMER_BLOCK).build()
    );

    public static void initialize() {
    }
}
