package org.rubix.redfile.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.rubix.redfile.ModEntity;
import org.rubix.redfile.client.render.entity.BlockHighlighterRenderer;

public class ModClientEntity {
    public static void init() {
        EntityRendererRegistry.register(ModEntity.BLOCK_HIGHLIGHTER_ENTITY_ENTITY_TYPE, BlockHighlighterRenderer::new);
    }
}
