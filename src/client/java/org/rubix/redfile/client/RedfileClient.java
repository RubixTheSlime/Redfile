package org.rubix.redfile.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.impl.renderer.RendererManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.rubix.redfile.Redfile;

public class RedfileClient implements ClientModInitializer {
	private static final Identifier EXAMPLE_LAYER = Identifier.of(Redfile.MOD_ID, "hud-example-layer");

    @Override
    public void onInitializeClient() {
		ModClientEntity.init();

//        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, EXAMPLE_LAYER, RedfileClient::render));
    }



	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		int color = 0xFFFF0000; // Red
		int targetColor = 0xFF00FF00; // Green
//		RenderSystem.lineWidth(0.1f);
//		RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
////		RenderSystem.setProjectionMatrix(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionType());
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//		BuiltBuffer builtBuffer;
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
//		MinecraftClient mc = MinecraftClient.getInstance();
//		var cameraPos = mc.gameRenderer.getCamera().getPos();
//		float x = (float) cameraPos.x;
//		float y = (float) cameraPos.y;
//		float z = (float) cameraPos.z;
//
//		buffer.vertex(100.0f - x, 100.0f - y, 100.0f - z).color(targetColor);
//		buffer.vertex(100.0f - x, 100.0f - y, 110.0f - z).color(targetColor);
//		buffer.vertex(110.0f - x, 100.0f - y, 110.0f - z).color(targetColor);
//		buffer.vertex(110.0f - x, 100.0f - y, 100.0f - z).color(targetColor);
//		try {
//			builtBuffer = buffer.end();
//			BufferRenderer.drawWithGlobalProgram(builtBuffer);
//			builtBuffer.close();
//		} catch (Exception ignored) {}

//		var matrix = context.getMatrices().peek();
		context.draw((ver) -> {
			var a = ver.getBuffer(RenderLayer.LINES);
			a.vertex(0,0,0).normal(0,1, 0).color(color);
			a.vertex(100,100,100).normal(0,1,0).color(targetColor);
		});


		// You can use the Util.getMeasuringTimeMs() function to get the current time in milliseconds.
		// Divide by 1000 to get seconds.
		double currentTime = Util.getMeasuringTimeMs() / 1000.0;

		// "lerp" simply means "linear interpolation", which is a fancy way of saying "blend".
		float lerpedAmount = MathHelper.abs(MathHelper.sin((float) currentTime));
		int lerpedColor = ColorHelper.lerp(lerpedAmount, color, targetColor);

		// Draw a square with the lerped color.
		// x1, x2, y1, y2, z, color
		context.fill(0, 0, 10, 10, 0, lerpedColor);
//		context.drawText(mc.textRenderer, "AAAAAA", 10,10, 0xFFFFFFFF, false);
	}
}
