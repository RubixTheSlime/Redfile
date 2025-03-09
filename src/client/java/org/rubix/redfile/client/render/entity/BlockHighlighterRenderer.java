package org.rubix.redfile.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.rubix.redfile.client.render.ModRenderLayer;
import org.rubix.redfile.client.render.entity.state.BlockHighlighterEntityRenderState;
import org.rubix.redfile.entity.BlockHighlighterEntity;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class BlockHighlighterRenderer extends EntityRenderer<BlockHighlighterEntity, BlockHighlighterEntityRenderState> {
    public BlockHighlighterRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BlockHighlighterEntityRenderState createRenderState() {
        return new BlockHighlighterEntityRenderState();
    }


    @Override
    public void updateRenderState(BlockHighlighterEntity entity, BlockHighlighterEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity,state,tickDelta);
        int color = entity.getColor();
        state.render = entity.getEnabled();
        if (state.render) {
            state.color = color;
            state.connections = entity.getConnections();
        }
    }

    @Override
    public void render(BlockHighlighterEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!state.render) return;
        matrices.push();
        matrices.translate(0, 9f/16, 0);
        var matrix = matrices.peek().getPositionMatrix();

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ModRenderLayer.HIGHLIGHT);

        float u = getDepth(state, Direction.UP);
        float d = getDepth(state, Direction.DOWN);
        float e = getDepth(state, Direction.EAST);
        float w = getDepth(state, Direction.WEST);
        float n = getDepth(state, Direction.NORTH);
        float s = getDepth(state, Direction.SOUTH);

        // up
        vertexConsumer.vertex(matrix, e, u, s).color(state.color);
        vertexConsumer.vertex(matrix, e, u, n).color(state.color);
        vertexConsumer.vertex(matrix, w, u, n).color(state.color);
        vertexConsumer.vertex(matrix, w, u, s).color(state.color);

        // down
        vertexConsumer.vertex(matrix, w, d, s).color(state.color);
        vertexConsumer.vertex(matrix, w, d, n).color(state.color);
        vertexConsumer.vertex(matrix, e, d, n).color(state.color);
        vertexConsumer.vertex(matrix, e, d, s).color(state.color);

        // east
        vertexConsumer.vertex(matrix, e, d, s).color(state.color);
        vertexConsumer.vertex(matrix, e, d, n).color(state.color);
        vertexConsumer.vertex(matrix, e, u, n).color(state.color);
        vertexConsumer.vertex(matrix, e, u, s).color(state.color);

        // west
        vertexConsumer.vertex(matrix, w, u, s).color(state.color);
        vertexConsumer.vertex(matrix, w, u, n).color(state.color);
        vertexConsumer.vertex(matrix, w, d, n).color(state.color);
        vertexConsumer.vertex(matrix, w, d, s).color(state.color);

        // south
        vertexConsumer.vertex(matrix, e, d, s).color(state.color);
        vertexConsumer.vertex(matrix, e, u, s).color(state.color);
        vertexConsumer.vertex(matrix, w, u, s).color(state.color);
        vertexConsumer.vertex(matrix, w, d, s).color(state.color);

        // north
        vertexConsumer.vertex(matrix, e, u, n).color(state.color);
        vertexConsumer.vertex(matrix, e, d, n).color(state.color);
        vertexConsumer.vertex(matrix, w, d, n).color(state.color);
        vertexConsumer.vertex(matrix, w, u, n).color(state.color);

        matrices.pop();
//        super.render(state, matrices, vertexConsumers, light);
    }

    private float getDepth(BlockHighlighterEntityRenderState state, Direction direction) {
        float res = ((state.connections & (byte) (1 << direction.getId())) == 0) ? 0.498f : 0.502f;
        return switch (direction) {
            case UP, EAST, SOUTH -> res;
            default -> -res;
        };
    }
}
