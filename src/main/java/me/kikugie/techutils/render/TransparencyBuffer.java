package me.kikugie.techutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL30;

/**
 * Taken from <a href="https://modrinth.com/plugin/cae2">Client Storage</a> by samolego.
 *
 * @see <a href="https://github.com/samolego/ClientStorage/blob/master/fabric-client/src/main/java/org/samo_lego/clientstorage/fabric_client/render/TransparencyBuffer.java">TransparencyBuffer.java</a>
 */
public class TransparencyBuffer {
    private static final Framebuffer framebuffer;
    private static int previousFramebuffer;

    static {
        Window window = MinecraftClient.getInstance().getWindow();
        framebuffer = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.setClearColor(0, 0, 0, 0);
    }

    public static void prepareExtraFramebuffer() {
        // Setup extra framebuffer to draw into
        previousFramebuffer = GlStateManager.getBoundFramebuffer();
        framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.beginWrite(false);
    }

    public static void preInject() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
    }

    public static void drawExtraFramebuffer(MatrixStack matrices) {
        // Restore the original framebuffer
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);

        // Render the custom framebuffer's contents with transparency into the main buffer
        RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
        Window window = MinecraftClient.getInstance().getWindow();
        // Create new matrix stack to prevent the transparency from affecting the rest of the GUI
        DrawableHelper.drawTexture(matrices,
                0,
                0,
                window.getScaledWidth(),
                window.getScaledHeight(),
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
        framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
    }
}
