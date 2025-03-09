package org.rubix.redfile.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class BlockHighlighterEntityRenderState extends EntityRenderState {
    public boolean render;
    public int color;
    public byte connections;
}
