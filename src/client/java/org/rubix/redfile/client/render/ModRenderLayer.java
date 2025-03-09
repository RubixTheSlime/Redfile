package org.rubix.redfile.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import static net.minecraft.client.render.RenderPhase.*;


public class ModRenderLayer {
    public static final RenderLayer HIGHLIGHT = RenderLayer.of(
        "highlight",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.QUADS,
        786432,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().program(POSITION_COLOR_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).build(false)

    );
}
