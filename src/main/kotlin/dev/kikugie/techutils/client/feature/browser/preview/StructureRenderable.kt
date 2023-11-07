package dev.kikugie.techutils.client.feature.browser.preview

import com.glisco.isometricrenders.render.AreaRenderable
import com.glisco.isometricrenders.render.Renderable
import com.glisco.isometricrenders.render.RenderableDispatcher
import dev.kikugie.techutils.client.feature.browser.BrowserConfig
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.impl.structure.world.StructureWorld
import dev.kikugie.techutils.client.util.render.ScissorStack
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.worldmesher.WorldMesh
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import kotlin.math.cos
import kotlin.math.tan

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

        // TODO: Calculate model scalar
        val width = dims.lengthX * cos(angleRad) + dims.lengthZ * cos(angleRad)
        val height = dims.lengthY + dims.lengthX * tan(slantRad) + dims.lengthZ * tan(slantRad)
    }

    private fun loadProperties() {
        properties().rotation.set(BrowserConfig.previewAngle.integerValue)
        properties().slant.set(BrowserConfig.previewSlant.integerValue)
    }

    fun drawModel(x: Int, y: Int, size: Int): Boolean {
        this.x = x
        this.y = y
        this.size = size

        val tickDelta = client.tickDelta
        val window = client.window

        if (!mesh.canRender())
            return false

        RenderableDispatcher.drawIntoActiveFramebuffer(
            this,
            window.framebufferWidth / window.framebufferHeight.toFloat(),
            tickDelta
        ) { stack: MatrixStack ->
            translate(stack, (x + dx + size / 2).toInt(), (y + dy + size / 2).toInt())
        }
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

    private fun translate(matrices: MatrixStack, x: Int, y: Int) {
        val screen = client.currentScreen!!
        val w = screen.width
        val h = screen.height
        matrices.translate((2F * x - w) / h, -(2F * y - h) / h, 0F)
    }

    companion object {
        fun from(structure: Structure, scissors: ScissorStack): StructureRenderable {
            val size = BlockPos(structure.metadata.size).subtract(Vec3i(1, 1, 1))
            val mesh = WorldMesh.Builder(
                structure.world,
                BlockPos.ORIGIN,
                size)
            //#if MC > 12000
            { _, _, _ -> structure.world.allEntities }
            //#else
            //$$ { _ -> structure.world.allEntities }
            //#endif
            return StructureRenderable(mesh.build(), structure.world, scissors)
        }
    }

    class LitematicPropertyBundle : AreaPropertyBundle() {
        override fun buildGuiControls(renderable: Renderable<*>?, container: FlowLayout) {
        }
    }
}
