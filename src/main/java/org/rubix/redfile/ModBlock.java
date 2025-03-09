package org.rubix.redfile;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.rubix.redfile.block.RedfilePrimerBlock;

import java.util.ArrayList;
import java.util.function.Function;

import static org.rubix.redfile.Redfile.MOD_ID;

public class ModBlock {
    public static final Block REDFILE_PRIMER_BLOCK = register("redfile_primer_block", RedfilePrimerBlock::new);

    private static Block register(String id, @NotNull Function<AbstractBlock.Settings, Block> factory) {
        Identifier full_id = Identifier.of(MOD_ID, id);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, full_id);

        Block.Settings settings = AbstractBlock.Settings.create()
            .nonOpaque()
            .sounds(BlockSoundGroup.STONE)
            .allowsSpawning(Blocks::never)
            .registryKey(key)
            ;

        Block block = Registry.register(Registries.BLOCK, key, factory.apply(settings));

        RegistryKey<Item> item_key = RegistryKey.of(RegistryKeys.ITEM, full_id);

        Item.Settings item_settings = new Item.Settings()
            .useBlockPrefixedTranslationKey()
            .registryKey(item_key);

        Registry.register(Registries.ITEM, item_key, new BlockItem(block, item_settings));

        return block;
    }

    public static void initialize() {
    }
}
