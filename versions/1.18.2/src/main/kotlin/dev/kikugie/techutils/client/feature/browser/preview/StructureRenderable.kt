package dev.kikugie.techutils.client.feature.browser.preview

import com.glisco.isometricrenders.render.AreaRenderable
import com.glisco.isometricrenders.render.Renderable
import com.glisco.isometricrenders.render.RenderableDispatcher
import com.glisco.isometricrenders.widget.WidgetColumnBuilder
import dev.kikugie.techutils.client.feature.browser.BrowserConfig
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.impl.structure.world.StructureWorld
import dev.kikugie.techutils.client.util.render.ScissorStack
import io.wispforest.worldmesher.WorldMesh
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
data class StructureRenderable(
    val mesh: WorldMesh,
    val world: StructureWorld,
    val scissors: ScissorStack
) : AreaRenderable(mesh) {
    private val properties = LitematicPropertyBundle()
    private val client = MinecraftClient.getInstance()
    private var x = 0
    private var y = 0
    private var size = 0

    private var dx = 0.0
    private var dy = 0.0

    init {
        loadProperties()
        val dims = mesh.dimensions()
        val angleRad = properties.rotation.get().toDouble() * MathHelper.RADIANS_PER_DEGREE
        val slantRad = properties.slant.get().toDouble() * MathHelper.RADIANS_PER_DEGREE
    }

    private fun loadProperties() {
        properties().rotation.set(BrowserConfig.previewAngle.integerValue)
        properties().slant.set(BrowserConfig.previewSlant.integerValue)
    }

    fun drawModel(x: Int, y: Int, size: Int): Boolean {
        this.x = x
        this.y = y
        this.size = size

        properties.x = x
        properties.y = y
        properties.dx = dx
        properties.dy = dy
        properties.size = size

        val tickDelta = client.tickDelta
        val window = client.window

        if (!mesh.canRender())
            return false

        RenderableDispatcher.drawIntoActiveFramebuffer(
            this,
            window.framebufferWidth / window.framebufferHeight.toFloat(),
            tickDelta
        )
        return true
    }

    override fun prepare() {
        scissors.pushDirect(x, y, size, size)
        scissors.enable()
        super.prepare()
    }

    override fun cleanUp() {
        scissors.disable()
        scissors.pop()
        super.cleanUp()
    }

    override fun properties(): LitematicPropertyBundle {
        return properties
    }

    fun shift(dx: Double, dy: Double) {
        this.dx += dx
        this.dy += dy
    }

    companion object {
        fun from(structure: Structure, scissors: ScissorStack): StructureRenderable {
            val size = BlockPos(structure.metadata.size).subtract(Vec3i(1, 1, 1))
            val mesh = WorldMesh.Builder(
                structure.world,
                BlockPos.ORIGIN,
                size
            )
            return StructureRenderable(mesh.build(), structure.world, scissors)
        }
    }

    class LitematicPropertyBundle : AreaPropertyBundle() {
        var x = 0
        var dx = 0.0
        var y = 0
        var dy = 0.0
        var size = 0
        override fun buildGuiControls(renderable: Renderable<*>?, builder: WidgetColumnBuilder?) {
        }

        override fun applyToViewMatrix(stack: MatrixStack) {
            translate(stack, (x + dx + size / 2.0).toInt(), (y + dy + size / 2.0).toInt())
            super.applyToViewMatrix(stack)
        }

        private fun translate(matrices: MatrixStack, x: Int, y: Int) {
            val screen = MinecraftClient.getInstance().currentScreen!!
            val w = screen.width
            val h = screen.height
            matrices.translate((2.0 * x - w) / h, -(2.0 * y - h) / h, 0.0)
        }
    }
}
