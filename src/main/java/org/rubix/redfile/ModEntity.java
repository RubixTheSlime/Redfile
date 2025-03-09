package org.rubix.redfile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.rubix.redfile.entity.BlockHighlighterEntity;

import static org.rubix.redfile.Redfile.MOD_ID;

public class ModEntity {
    public static EntityType<BlockHighlighterEntity> BLOCK_HIGHLIGHTER_ENTITY_ENTITY_TYPE;

    public static void init() {

        Identifier id = Identifier.of(MOD_ID, "block_highlighter");
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);

        BLOCK_HIGHLIGHTER_ENTITY_ENTITY_TYPE = EntityType.Builder
                .create(BlockHighlighterEntity::new, SpawnGroup.MISC)
                .dimensions(1.125f, 1.125f)
                .build(key);
        Registry.register(Registries.ENTITY_TYPE, key, BLOCK_HIGHLIGHTER_ENTITY_ENTITY_TYPE);
    }
}
