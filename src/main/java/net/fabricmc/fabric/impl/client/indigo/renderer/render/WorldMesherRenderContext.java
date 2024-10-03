package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class WorldMesherRenderContext extends AbstractBlockRenderContext {

	private final BlockRenderView blockView;
	private final Function<RenderLayer, VertexConsumer> bufferFunc;

	public WorldMesherRenderContext(BlockRenderView blockView, Function<RenderLayer, VertexConsumer> bufferFunc) {
		this.blockView = blockView;
		this.bufferFunc = bufferFunc;

		this.blockInfo.prepareForWorld(blockView, true);
		this.blockInfo.random = Random.create();
	}

	public void tessellateBlock(BlockRenderView blockView, BlockState blockState, BlockPos blockPos, final BakedModel model, MatrixStack matrixStack) {
		try {
			Vec3d vec3d = blockState.getModelOffset(blockView, blockPos);
			matrixStack.translate(vec3d.x, vec3d.y, vec3d.z);

			this.matrix = matrixStack.peek().getPositionMatrix();
			this.normalMatrix = matrixStack.peek().getNormalMatrix();

			blockInfo.recomputeSeed = true;

			aoCalc.clear();
			blockInfo.prepareForBlock(blockState, blockPos, model.useAmbientOcclusion());
			model.emitBlockQuads(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, blockInfo.randomSupplier, this);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block in WorldMesher mesh");
			CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, blockView, blockPos, blockState);
			throw new CrashException(crashReport);
		}
	}

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return WorldRenderer.getLightmapCoordinates(WorldMesherRenderContext.this.blockView, state, pos);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return AoLuminanceFix.INSTANCE.apply(WorldMesherRenderContext.this.blockView, pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return this.bufferFunc.apply(layer);
	}
}
