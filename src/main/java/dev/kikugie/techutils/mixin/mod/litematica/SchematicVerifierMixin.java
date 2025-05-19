package dev.kikugie.techutils.mixin.mod.litematica;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.feature.containerscan.verifier.BlockMismatchExtension;
import dev.kikugie.techutils.feature.containerscan.verifier.SchematicVerifierExtension;
import dev.kikugie.techutils.util.ItemPredicateUtils;
import dev.kikugie.techutils.util.EntitiesDataStorage ;
import fi.dy.masa.litematica.scheduler.tasks.TaskBase;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.WorldUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import static fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import static fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;

@Mixin(value = SchematicVerifier.class, remap = false)
public abstract class SchematicVerifierMixin<InventoryBE extends BlockEntity & Inventory> extends TaskBase implements SchematicVerifierExtension {
	@Shadow @Final private static BlockPos.Mutable MUTABLE_POS;
	@Shadow private SchematicPlacement schematicPlacement;
	@Shadow private ClientWorld worldClient;

	@Shadow protected abstract void addAndSortPositions(MismatchType type, ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> sourceMap, List<BlockPos> listOut, int maxEntries);

	@Unique
	private final Set<Pair<InventoryBE, InventoryBE>> wrongInventories = new ReferenceOpenHashSet<>();
	@Unique
	private final ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> wrongInventoriesPositions = ArrayListMultimap.create();
	@Unique
	private final List<BlockPos> wrongInventoriesPositionsClosest = new ArrayList<>();
	@Unique
	private final List<BlockMismatch> selectedInventoryMismatches = new ArrayList<>();

	@Override
	public List<BlockMismatch> getSelectedInventoryMismatches$techutils() {
		return Collections.unmodifiableList(selectedInventoryMismatches);
	}

	@Override
	public int getWrongInventoriesCount$techutils() {
		return wrongInventories.size();
	}

	@ModifyExpressionValue(method = "verifyChunks", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/world/ChunkManagerSchematic;isChunkLoaded(II)Z", remap = true))
	private boolean ensureInventoriesAreLoaded(boolean isLoaded, @Local ChunkPos pos) {
		return isLoaded && canProcessChunk(pos);
	}

	@Redirect(
		method = "verifyChunks",
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/world/ChunkManagerSchematic;isChunkLoaded(II)Z")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/world/ClientWorld;getChunk(II)Lnet/minecraft/world/chunk/WorldChunk;",
			ordinal = 0,
			remap = true
		)
	)
	private WorldChunk pickBestWorld(ClientWorld clientWorld, int x, int z) {
		var bestWorld = WorldUtils.getBestWorld(mc);
		return (bestWorld != null ? bestWorld : clientWorld).getChunk(x, z);
	}

	/**
	 * Basically a clone of {@link fi.dy.masa.litematica.scheduler.tasks.TaskSaveSchematic#canProcessChunk}
	 */
	@Unique
	private boolean canProcessChunk(ChunkPos pos)
	{
		// Request entity data from Servux, if the ClientWorld matches, and treat it as not yet loaded
		EntitiesDataStorage eds = EntitiesDataStorage.getInstance();
		if ((eds.hasPcaServer() || eds.getIfReceivedBackupPackets())
			&& Objects.equals(eds.getWorld(), this.worldClient)
			&& !eds.hasCompletedChunk(pos))
		{
			if (eds.hasPendingChunk(pos))
				return false;

			ImmutableMap<String, IntBoundingBox> volumes = schematicPlacement.getBoxesWithinChunk(pos.x, pos.z);
			int minY = 319;         // Invert Values
			int maxY = -64;

			for (Map.Entry<String, IntBoundingBox> volumeEntry : volumes.entrySet())
			{
				IntBoundingBox bb = volumeEntry.getValue();

				minY = Math.min(bb.minY, minY);
				maxY = Math.max(bb.maxY, maxY);
			}

			PcaSyncProtocol.cancelSyncBlockEntity();

			eds.requestBackupBulkEntityData(pos, minY, maxY);

			return false;
		}

		return this.areSurroundingChunksLoaded(pos, this.worldClient, 0);
	}

	@Inject(method = "verifyChunk", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier;checkBlockStates(IIILnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V", remap = true))
	private void checkInventories(Chunk chunkClient, Chunk chunkSchematic, IntBoundingBox box, CallbackInfoReturnable<Boolean> cir) {
		var expectedBE = chunkSchematic.getBlockEntity(MUTABLE_POS);
		var foundBE = chunkClient.getBlockEntity(MUTABLE_POS);
		if (!(expectedBE instanceof Inventory expected && foundBE instanceof Inventory found)
			|| expectedBE.getType() != foundBE.getType()) {
			return;
		}

		int size = expected.size();
		if (size != found.size()) {
			return;
		}

		var itemsForStates = ItemUtilsAccessor.getItemsForStates();
		boolean verifyNbt = LitematicConfigs.VERIFY_ITEM_NBT.getBooleanValue();
		for (int i = size - 1; i >= 0; i--) {
			var expectedStack = expected.getStack(i);
			var foundStack = found.getStack(i);

			Boolean predFailed = null;
			var predicate = ItemPredicateUtils.getPredicate(expectedStack);
			if (predicate != null) {
				predFailed = !predicate.test(foundStack);
			}

			if (predFailed != null
				? predFailed
				: expectedStack.getItem() != foundStack.getItem()
					|| expectedStack.getCount() != foundStack.getCount()
					|| verifyNbt
					&& !Objects.equals(expectedStack.getNbt(), foundStack.getNbt())
			) {
				var pos = MUTABLE_POS.toImmutable();
				//noinspection unchecked
				var pair = populateTooltipsIfNecessary((InventoryBE) expected, (InventoryBE) found, verifyNbt);
				wrongInventories.add(pair);
				warCrime(pair.getLeft(), pair.getRight(), itemsForStates, pos);
				break;
			}
		}
	}

	/**
	 * I ask for your forgiveness, future viewer (this makes differentiating inventories with the same block state possible)
	 */
	@Unique
	private void warCrime(InventoryBE expected, InventoryBE found, IdentityHashMap<BlockState, ItemStack> itemsForStates, BlockPos pos) {
		BlockState foundState = found.getCachedState();
		HashMap<Property<?>, Comparable<?>> propertyMap = new HashMap<>(foundState.getEntries());

		propertyMap.put(BooleanProperty.of("war_crime"), true);
		BlockState newState = new BlockState(foundState.getBlock(), ImmutableMap.copyOf(new Reference2ObjectArrayMap<>(propertyMap)), null);

		itemsForStates.put(newState, ItemUtils.getItemForBlock(worldClient, pos, foundState, true));
		wrongInventoriesPositions.put(Pair.of(expected.getCachedState(), newState), pos);
		found.setCachedState(newState);
	}

	@SuppressWarnings("unchecked")
	@Unique
	private Pair<InventoryBE, InventoryBE> populateTooltipsIfNecessary(InventoryBE expected, InventoryBE found, boolean verifyNbt) {
		final var expectedNew = (InventoryBE) BlockEntity.createFromNbt(expected.getPos(), expected.getCachedState(), expected.createNbtWithIdentifyingData());
		final var foundNew = (InventoryBE) BlockEntity.createFromNbt(found.getPos(), found.getCachedState(), found.createNbtWithIdentifyingData());
		int size = expected.size();
		for (int i = size - 1; i >= 0; i--) {
			var expectedStack = expectedNew.getStack(i);
			var foundStack = foundNew.getStack(i);


			var predicate = ItemPredicateUtils.getPredicate(expectedStack);
			if (predicate != null) {
				expectedStack.setCustomName(Text.literal("Item Predicate")
						.styled(style -> style.withColor(Formatting.WHITE).withItalic(false)));

				foundStack.getOrCreateNbt().put(ERROR_LINES_ID, ERROR_LINES_CODEC
					.encodeStart(NbtOps.INSTANCE, ItemPredicateUtils.getErrorLines(foundStack, predicate))
					.getOrThrow(false, s -> {}));
				continue;
			}

			if (verifyNbt && !Objects.equals(expectedStack.getNbt(), foundStack.getNbt())) {
				foundStack.getOrCreateNbt().put(ERROR_LINES_ID, ERROR_LINES_CODEC
					.encodeStart(NbtOps.INSTANCE, List.of(Text.literal("Item NBT doesn't match!")
						.styled(style -> style.withColor(Formatting.RED).withItalic(false))))
					.getOrThrow(false, s -> {}));
			}
		}
		return Pair.of(expectedNew, foundNew);
	}

	@Inject(method = "addCountFor", at = @At("HEAD"), cancellable = true)
	private void addCountForWrongInventories(MismatchType mismatchType, ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> map, List<BlockMismatch> list, CallbackInfo ci) {
		if (mismatchType != WRONG_INVENTORIES) {
			return;
		}

		for (var pair : wrongInventories) {
			BlockState leftState = pair.getLeft().getCachedState();
			BlockState rightState = pair.getRight().getCachedState();
			BlockMismatch blockMismatch = new BlockMismatch(WRONG_INVENTORIES, leftState, rightState, 1);
			//noinspection unchecked
			((BlockMismatchExtension<InventoryBE>) blockMismatch).setInventories$techutils(pair);
			list.add(blockMismatch);
		}
		ci.cancel();
	}

	@Inject(method = "toggleMismatchEntrySelected", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/HashMultimap;remove(Ljava/lang/Object;Ljava/lang/Object;)Z"))
	private void tryRemoveSelectedInventoryMismatch(BlockMismatch mismatch, CallbackInfo ci, @Local MismatchType type) {
		if (type == WRONG_INVENTORIES) {
			selectedInventoryMismatches.remove(mismatch);
		}
	}

	@Inject(method = "toggleMismatchEntrySelected", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/HashMultimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z"))
	private void tryAddSelectedInventoryMismatch(BlockMismatch mismatch, CallbackInfo ci, @Local MismatchType type) {
		if (type == WRONG_INVENTORIES) {
			selectedInventoryMismatches.add(mismatch);
		}
	}

	@Inject(method = "removeSelectedEntriesOfType", at = @At("HEAD"))
	private void tryRemoveSelectedInventoryMismatches(MismatchType type, CallbackInfo ci) {
		if (type == WRONG_INVENTORIES) {
			selectedInventoryMismatches.clear();
		}
	}

	@WrapOperation(method = "getMismatchOverviewCombined", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier;addCountFor(Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier$MismatchType;Lcom/google/common/collect/ArrayListMultimap;Ljava/util/List;)V", ordinal = 0))
	private void updateClosestWrongInventoriesPositions(SchematicVerifier instance, MismatchType type, ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> positions, List<BlockMismatch> list, Operation<Void> original) {
		original.call(instance, WRONG_INVENTORIES, wrongInventoriesPositions, list);
		original.call(instance, type, positions, list);
	}

	@Inject(method = "updateClosestPositions", at = @At("TAIL"))
	private void updateClosestWrongInventoriesPositions(BlockPos centerPos, int maxEntries, CallbackInfo ci) {
		addAndSortPositions(WRONG_INVENTORIES, wrongInventoriesPositions, wrongInventoriesPositionsClosest, maxEntries);
	}

	@WrapOperation(method = "combineClosestPositions", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier;getMismatchRenderPositionFor(Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier$MismatchType;Ljava/util/List;)V", ordinal = 0))
	private void updateClosestWrongInventoriesPositions(SchematicVerifier instance, MismatchType type, List<MismatchRenderPos> tempList, Operation<Void> original) {
		original.call(instance, WRONG_INVENTORIES, tempList);
		original.call(instance, type, tempList);
	}

	@Inject(method = "getMapForMismatchType", at = @At("HEAD"), cancellable = true)
	private void addWrongInventoriesMap(MismatchType mismatchType, CallbackInfoReturnable<ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos>> cir) {
		if (mismatchType == WRONG_INVENTORIES) {
			cir.setReturnValue(wrongInventoriesPositions);
		}
	}

	@Inject(method = "getClosestMismatchedPositionsFor", at = @At("HEAD"), cancellable = true)
	private void addWrongInventoriesMismatchedPositions(MismatchType type, CallbackInfoReturnable<List<BlockPos>> cir) {
		if (type == WRONG_INVENTORIES) {
			cir.setReturnValue(wrongInventoriesPositionsClosest);
		}
	}

	@ModifyExpressionValue(method = "ignoreStateMismatch(Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier$BlockMismatch;Z)V", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier;getMapForMismatchType(Lfi/dy/masa/litematica/schematic/verifier/SchematicVerifier$MismatchType;)Lcom/google/common/collect/ArrayListMultimap;"))
	private ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> removeInventoryIfNecessary(ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> positions, @Local(argsOnly = true) BlockMismatch mismatch) {
		if (positions == wrongInventoriesPositions) {
			wrongInventories.remove(((BlockMismatchExtension<?>) mismatch).getInventories$techutils());
			selectedInventoryMismatches.remove(mismatch);
		}
		return positions;
	}

	@Inject(method = "clearData", at = @At("HEAD"))
	private void clearAdditionalData(CallbackInfo ci) {
		var itemsForStates = ItemUtilsAccessor.getItemsForStates();
		for (Pair<BlockState, BlockState> pair : wrongInventoriesPositions.keySet()) {
			itemsForStates.remove(pair.getRight());
		}
		wrongInventories.clear();
		wrongInventoriesPositions.clear();
		selectedInventoryMismatches.clear();
		EntitiesDataStorage.getInstance().reset(false);
	}
}
