package me.kikugie.techutils.render.litematica

import com.mojang.blaze3d.systems.RenderSystem
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.litematica.util.WorldUtils
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.Color4f
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

class LitematicRenderer(
    entry: WidgetFileBrowserBase.DirectoryEntry,
    gui: GuiSchematicBrowserBase?,
    private val slant: Int
) {
    private val client = MinecraftClient.getInstance()
    var angle = 135
    private val mesh: LitematicMesh?
    private val horizontalSize: Double
    private val verticalSize: Double
    private var progressBarCooldown = 30

    init {
        val schematic: LitematicaSchematic?
        val fileType = FileType.fromFile(entry.fullPath)
        schematic = when (fileType) {
            FileType.LITEMATICA_SCHEMATIC -> LitematicaSchematic.createFromFile(
                entry.directory,
                entry.name
            )

//            FileType.SCHEMATICA_SCHEMATIC -> WorldUtils.convertSchematicaSchematicToLitematicaSchematic(
//                entry.directory,
//                entry.name,
//                false,
//                gui
//            )
//
//            FileType.SPONGE_SCHEMATIC -> WorldUtils.convertSpongeSchematicToLitematicaSchematic(
//                entry.directory,
//                entry.name
//            )
//
//            FileType.VANILLA_STRUCTURE -> WorldUtils.convertStructureToLitematicaSchematic(
//                entry.directory,
//                entry.name
//            )

            else -> null
        }
        mesh = LitematicMesh(schematic, true)
        val shortestSide = min(mesh.size.x, mesh.size.z)
        val longestSide = max(mesh.size.x, mesh.size.z)
        horizontalSize = shortestSide * MAX_BLOCK_WIDTH + longestSide - shortestSide
        verticalSize = longestSide * tan(Math.toRadians(slant.toDouble())) + mesh.size.y
    }

    fun render(matrices: MatrixStack, x: Int, y: Int, size: Int) {
        if (mesh == null) return
        RenderUtils.drawOutlinedBox(x, y, size, size, -1610612736, -6710887)
        if (mesh.isComplete) {
            renderMesh(matrices, x, y, size)
        } else {
            renderProgressBar(x, y, size)
        }
    }

    private fun renderProgressBar(x: Int, y: Int, size: Int) {
        if (progressBarCooldown > 0) {
            progressBarCooldown--
            return
        }
        val barWidth = size - 4
        val barHeight = 8
        val barX = x + 2
        val barY = y + size / 2 - 4
        val fill = ((mesh?.completion ?: 0f) * (barWidth - 2)).toInt()
        RenderUtils.drawOutlinedBox(barX, barY, barWidth, barHeight, -1610612736, -6710887)
        RenderUtils.drawRect(barX + 1, barY + 1, fill, barHeight - 2, BAR_COLOR.intValue)
    }

    private fun renderMesh(matrices: MatrixStack, x: Int, y: Int, size: Int) {
        val window = MinecraftClient.getInstance().window
        val aspectRatio = window.framebufferWidth / window.framebufferHeight.toFloat()
        RenderSystem.backupProjectionMatrix()
        val projectionMatrix = Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1f, 1f, -1000f, 3000f)
        RenderSystem.setProjectionMatrix(projectionMatrix)
        matrices.push()
        matrices.loadIdentity()

//        matrices.translate(mesh.space.fullBox.minX, mesh.space.fullBox.minY, mesh.space.fullBox.minZ);
        translateToCoords(matrices, x + size / 2, y + size / 2)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(slant.toFloat()))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle.toFloat()))
        val scaleFactor = getScaleFactor(size)
        matrices.scale(scaleFactor, scaleFactor, scaleFactor)
        RenderSystem.applyModelViewMatrix()
        emitVertices()
        drawModel(matrices.peek().positionMatrix)
        matrices.pop()
        RenderSystem.applyModelViewMatrix()
        RenderSystem.restoreProjectionMatrix()
    }

    private fun drawModel(viewMatrix: Matrix4f) {
        draw(viewMatrix)
        val meshStack = MatrixStack()
        viewMatrix.translate(
            -mesh!!.space.fullBox.minX.toFloat(),
            -mesh.space.fullBox.minY.toFloat(),
            -mesh.space.fullBox.minZ.toFloat()
        )
        viewMatrix.translate(-mesh.size.x / 2f, -mesh.size.y / 2f, -mesh.size.z / 2f)
        meshStack.multiplyPositionMatrix(viewMatrix)
        mesh.render(meshStack)
    }

    private fun draw(viewMatrix: Matrix4f) {
        val lightTransform = Matrix4f(viewMatrix)
        val lightDirection = Vector4f(-0.5f, 0.35f, 1.0f, 0.0f)
        lightTransform.invert()
        lightDirection.mul(lightTransform)
        val transformedLightDirection = Vector3f(lightDirection.x, lightDirection.y, lightDirection.z)
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection)
        MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.draw()
    }

    private fun emitVertices() {
        // FIXME: 21/01/2023 Lighting is fucked up
        val matrices = MatrixStack()
        val vertexConsumers = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers

        matrices.loadIdentity()
        matrices.translate(-mesh!!.space.fullBox.minX, -mesh.space.fullBox.minY, -mesh.space.fullBox.minZ)
        matrices.translate(-mesh.size.x / 2f, -mesh.size.y / 2f, -mesh.size.z / 2f)

        mesh.blockEntities.forEach { (pos, be) ->
            matrices.push()
            matrices.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
            client.blockEntityRenderDispatcher.render(be, 0f, matrices, MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers)
            matrices.pop()
        }
        draw(RenderSystem.getModelViewMatrix())
    }

    private fun translateToCoords(matrixStack: MatrixStack, x: Int, y: Int) {
        val screen = MinecraftClient.getInstance().currentScreen
        val w = screen!!.width
        val h = screen.height
        matrixStack.translate((2f * x - w) / h, -(2f * y - h) / h, 0f)
    }

    private fun getScaleFactor(size: Int): Float {
        return (size * 2 / (max(
            horizontalSize,
            verticalSize
        ) * MinecraftClient.getInstance().currentScreen!!.height)).toFloat()
    }

    companion object {
        private val MAX_BLOCK_WIDTH = cos(Math.PI / 6) * 2
        private val BAR_COLOR = Color4f(0.25f, 1f, 0.25f, 1f)
    }
}