package me.kikugie.techutils.render.litematica

import com.mojang.blaze3d.systems.RenderSystem
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import io.wispforest.worldmesher.renderers.WorldMesherBlockModelRenderer
import io.wispforest.worldmesher.renderers.WorldMesherFluidRenderer
import me.kikugie.techutils.mixin.BlockEntityAccessor
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer
import net.fabricmc.fabric.impl.client.indigo.renderer.render.WorldMesherRenderContext
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.*
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.util.math.random.Random
import org.joml.Matrix4f
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class LitematicMesh(private val schematic: LitematicaSchematic?, async: Boolean) {
    val space: LitematicSpace = LitematicSpace(schematic)
    private val bufferStorage: MutableMap<RenderLayer, VertexBuffer>
    private val initializedLayers: MutableMap<RenderLayer, BufferBuilder>
    private val blockRenderManager: BlockRenderManager
    val blockEntities: HashMap<BlockPos, BlockEntity>
    private val client = MinecraftClient.getInstance()
    var completion = 0f
    var isComplete = false

    init {
        bufferStorage = HashMap()
        initializedLayers = HashMap()
        blockEntities = HashMap()
        blockRenderManager = client.blockRenderManager
        if (async) {
            CompletableFuture.runAsync({ this.build() }, Util.getMainWorkerExecutor())
                .whenComplete { _: Void?, throwable: Throwable? ->
                    if (throwable != null) {
                        throw RuntimeException(throwable)
                    }
                }
        } else {
            build()
        }
    }

    private fun build() {
        val matrices = MatrixStack()
        val renderContext =
            if (RendererAccess.INSTANCE.renderer is IndigoRenderer) WorldMesherRenderContext(client.world) { layer: RenderLayer ->
                getOrCreateBuffer(layer)
            } else null
        val blockRenderer = WorldMesherBlockModelRenderer()
        val fluidRenderer = WorldMesherFluidRenderer()
        val random = Random.createLocal()
        var processedBlocks = 0
        for (region in space.boxes.keys) {
            val box = space.boxes[region]!!
            val blockView = space.regionBlockViewMap[region]
            val origin = BlockPos(box.minX, box.minY, box.minZ)
            val end = BlockPos(box.maxX, box.maxY, box.maxZ)
            for (pos in BlockPos.iterate(origin, end)) {
                val state = blockView!!.getBlockState(pos)
                if (state.isAir) {
                    continue
                }

                val block = state.block
                if (block is BlockEntityProvider) {
                    val be = block.createBlockEntity(pos, state)!!
                    populateBlockEntity(be, state)
                    blockEntities[pos.toImmutable()] = be
                }

                if (!state.fluidState.isEmpty) {
                    val fluidState = state.fluidState
                    val fluidLayer = RenderLayers.getFluidLayer(fluidState)
                    matrices.push()
                    matrices.translate(-(pos.x and 15).toFloat(), -(pos.y and 15).toFloat(), -(pos.z and 15).toFloat())
                    matrices.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
                    fluidRenderer.setMatrix(matrices.peek().positionMatrix)
                    fluidRenderer.render(blockView, pos, getOrCreateBuffer(fluidLayer), state, fluidState)
                    matrices.pop()
                }

                matrices.push()
                matrices.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
                val model = blockRenderManager.getModel(state)
                val renderLayer = RenderLayers.getBlockLayer(state)
                if (!(model as FabricBakedModel).isVanillaAdapter && renderContext != null) {
                    renderContext.tessellateBlock(blockView, state, pos, model, matrices)
                } else if (state.renderType == BlockRenderType.MODEL) {
                    blockRenderer.render(
                        blockView,
                        model,
                        state,
                        pos,
                        matrices,
                        getOrCreateBuffer(renderLayer),
                        true,
                        random,
                        state.getRenderingSeed(pos),
                        OverlayTexture.DEFAULT_UV
                    )
                }
                matrices.pop()
                processedBlocks++
                completion = processedBlocks / schematic!!.metadata.totalBlocks.toFloat()
            }
        }
        if (initializedLayers.containsKey(RenderLayer.getTranslucent())) {
            val translucentBuilder = initializedLayers[RenderLayer.getTranslucent()]
            translucentBuilder!!.sortFrom(0f, 0f, 1000f)
        }
        val future = CompletableFuture<Void?>()
        RenderSystem.recordRenderCall {
            initializedLayers.forEach { (renderLayer: RenderLayer, bufferBuilder: BufferBuilder) ->
                val vertexBuffer = VertexBuffer()
                vertexBuffer.bind()
                vertexBuffer.upload(bufferBuilder.end())
                bufferStorage[renderLayer] = vertexBuffer
            }
            future.complete(null)
        }
        future.join()
        isComplete = true
    }

    fun render(matrices: MatrixStack) {
        val matrix = matrices.peek().positionMatrix
        val translucent = RenderLayer.getTranslucent()
        bufferStorage.forEach { (renderLayer: RenderLayer, vertexBuffer: VertexBuffer?) ->
            if (renderLayer === translucent) return@forEach
            draw(renderLayer, vertexBuffer, matrix)
        }
        if (bufferStorage.containsKey(translucent)) {
            draw(translucent, bufferStorage[translucent], matrix)
        }
        VertexBuffer.unbind()
    }

    private fun draw(renderLayer: RenderLayer, vertexBuffer: VertexBuffer?, matrix: Matrix4f) {
        renderLayer.startDrawing()
        vertexBuffer!!.bind()
        vertexBuffer.draw(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader())
        renderLayer.endDrawing()
    }

    private fun getOrCreateBuffer(layer: RenderLayer): VertexConsumer? {
        if (!initializedLayers.containsKey(layer)) {
            val builder = BufferBuilder(layer.expectedBufferSize)
            initializedLayers[layer] = builder
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL)
        }
        return initializedLayers[layer]
    }

    private fun populateBlockEntity(blockEntity: BlockEntity, state: BlockState?) {
        (blockEntity as BlockEntityAccessor).setCachedState(state)
        blockEntity.world = client.world
    }

    val size: Vec3i
        get() = schematic!!.totalSize
}