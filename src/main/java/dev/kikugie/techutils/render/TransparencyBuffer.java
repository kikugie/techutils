package dev.kikugie.techutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kikugie.techutils.mixin.containerscan.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;

/**
 * Taken from <a href="https://modrinth.com/plugin/cae2">Client Storage</a> by samolego.
 *
 * @see <a href="https://github.com/samolego/ClientStorage/blob/master/fabric-client/src/main/java/org/samo_lego/clientstorage/fabric_client/render/TransparencyBuffer.java">TransparencyBuffer.java</a>
 */
public class TransparencyBuffer {
	private static final Framebuffer framebuffer;
	private static Framebuffer builtinFramebuffer;

	static {
		var client = MinecraftClient.getInstance();
		Window window = client.getWindow();
		framebuffer = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true);
		framebuffer.setClearColor(0, 0, 0, 0);
		builtinFramebuffer = client.getFramebuffer();
	}

	public static void prepareExtraFramebuffer() {
		// Setup extra framebuffer to draw into
		framebuffer.clear();
		((MinecraftClientAccessor) MinecraftClient.getInstance()).setFramebuffer(framebuffer);
		framebuffer.beginWrite(false);
	}

	public static void preInject() {
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
	}

	public static void drawExtraFramebuffer(DrawContext context) {
		// Restore the original framebuffer
		((MinecraftClientAccessor) MinecraftClient.getInstance()).setFramebuffer(builtinFramebuffer);
		builtinFramebuffer.beginWrite(false);
		drawUntexturedQuad(context,
			0,
			0,
			context.getScaledWindowWidth(),
			context.getScaledWindowHeight(),
			0,
			framebuffer.textureHeight,
			framebuffer.textureWidth,
			-framebuffer.textureHeight,
			framebuffer.textureWidth,
			framebuffer.textureHeight);
	}

	public static void postInject() {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void resizeDisplay() {
		Window window = MinecraftClient.getInstance().getWindow();
		framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight());
	}

	public static void drawUntexturedQuad(DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		drawUntexturedQuad(context,
			x,
			x + width,
			y,
			y + height,
			0,
			(u + 0.0F) / (float) textureWidth,
			(u + (float) regionWidth) / (float) textureWidth,
			(v + 0.0F) / (float) textureHeight,
			(v + (float) regionHeight) / (float) textureHeight);
	}

	private static void drawUntexturedQuad(DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
		RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
		RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).texture(u1, v1);
		bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, (float) z).texture(u1, v2);
		bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) z).texture(u2, v2);
		bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, (float) z).texture(u2, v1);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		context.draw();
	}
}
