package dev.kikugie.techutils.client.feature.preview.render

import com.glisco.isometricrenders.render.AreaRenderable
import com.glisco.isometricrenders.render.Renderable
import com.glisco.isometricrenders.render.RenderableDispatcher
import com.mojang.blaze3d.systems.RenderSystem
import dev.kikugie.techutils.client.feature.preview.PreviewConfig
import dev.kikugie.techutils.client.feature.preview.world.LitematicRenderWorld
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.worldmesher.WorldMesh
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import net.minecraft.world.Difficulty
import java.util.concurrent.CompletableFuture
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.tan

data class LitematicRenderable(
    val mesh: WorldMesh,
    val litematic: LitematicaSchematic,
    val world: LitematicRenderWorld
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

        val width = dims.lengthX * cos(angleRad) + dims.lengthZ * cos(angleRad)
        val height = dims.lengthY + dims.lengthX * tan(slantRad) + dims.lengthZ * tan(slantRad)
    }

    private fun loadProperties() {
        properties().rotation.set(PreviewConfig.previewAngle.integerValue)
        properties().slant.set(PreviewConfig.previewSlant.integerValue)
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
        scissor(x + 1, y + 1, size - 2)
        super.prepare()
    }

    fun shift(dx: Double, dy: Double) {
        this.dx += dx
        this.dy += dy
    }

    private fun scissor(x: Int, y: Int, size: Int) {
        val window = client.window
        val d = window.scaleFactor
        val e = (x * d).toInt()
        val f = (window.framebufferHeight - (y + size) * d).toInt()
        val g = (size * d).toInt()
        RenderSystem.enableScissor(e, f, max(0, g), max(0, g))
    }

    private fun translate(matrices: MatrixStack, x: Int, y: Int) {
        val screen = client.currentScreen!!
        val w = screen.width
        val h = screen.height
        matrices.translate((2F * x - w) / h, -(2F * y - h) / h, 0F)
    }

    override fun cleanUp() {
        RenderSystem.disableScissor()
        super.cleanUp()
    }

    override fun properties(): LitematicPropertyBundle {
        return properties
    }

    companion object {
        fun from(entry: DirectoryEntry): CompletableFuture<LitematicRenderable>? {
            val litematic =
                LitematicaSchematic.createFromFile(entry.directory, entry.name, FileType.LITEMATICA_SCHEMATIC)
            return if (litematic != null) {
                of(litematic)
            } else {
                null
            }
        }

        fun of(litematic: LitematicaSchematic): CompletableFuture<LitematicRenderable> {
            val executor = Util.getMainWorkerExecutor()
            return CompletableFuture.supplyAsync({
                createWorld(litematic)
            }, executor).thenApplyAsync({
                createRenderable(litematic, it)
            }, executor)
        }

        private fun createWorld(litematic: LitematicaSchematic): LitematicRenderWorld {
            val client = MinecraftClient.getInstance()
            val placement = SchematicPlacement.createForSchematicConversion(litematic, BlockPos.ORIGIN)
            val world = LitematicRenderWorld(
                ClientWorld.Properties(Difficulty.PEACEFUL, false, true),
                client.world!!.dimensionEntry
            ) { client.profiler }
            world.loadChunks(placement.origin, litematic.metadata.enclosingSize)
            litematic.placeToWorld(world, placement, false, false)
            return world
        }

        private fun createRenderable(litematic: LitematicaSchematic, world: LitematicRenderWorld): LitematicRenderable {
            val size = BlockPos(litematic.metadata.enclosingSize).subtract(Vec3i(1, 1, 1))
            val mesh = WorldMesh.Builder(
                world,
                BlockPos.ORIGIN,
                size
            ) { _, _, _ -> world.allEntities }.build()
            return LitematicRenderable(mesh, litematic, world)
        }
    }

    class LitematicPropertyBundle : AreaPropertyBundle() {
        override fun buildGuiControls(renderable: Renderable<*>?, container: FlowLayout) {
        }
    }
}
