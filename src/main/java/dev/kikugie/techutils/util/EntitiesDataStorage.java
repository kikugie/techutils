package dev.kikugie.techutils.util;

import com.mojang.datafixers.util.Either;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.mixin.containerscan.DataQueryHandlerAccessor;
import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.util.InventoryUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntitiesDataStorage implements IClientTickHandler
{
	private static final EntitiesDataStorage INSTANCE = new EntitiesDataStorage();

	public static EntitiesDataStorage getInstance()
	{
		return INSTANCE;
	}

	private final static MinecraftClient mc = MinecraftClient.getInstance();
	//private int uptimeTicks = 0;
	private final long chunkTimeoutMs = 5000;
	// Wait 5 seconds for loaded Client Chunks to receive Entity Data
	private boolean checkOpStatus = true;
	private boolean hasOpStatus = false;
	private long lastOpCheck = 0L;

	// Data Cache
	private final ConcurrentHashMap<BlockPos, Pair<Long, Pair<BlockEntity, NbtCompound>>> blockEntityCache = new ConcurrentHashMap<>();
	private final long cacheTimeout = 4;
	private final long longCacheTimeout = 30;
	private boolean shouldUseLongTimeout = false;
	// Needs a long cache timeout for saving schematics
	private long serverTickTime = 0;
	// Requests to be executed
	private final Set<BlockPos> pendingBlockEntitiesQueue = new LinkedHashSet<>();
	private final Set<ChunkPos> pendingChunks = new LinkedHashSet<>();
	private final Set<ChunkPos> completedChunks = new LinkedHashSet<>();
	private final Map<ChunkPos, Long> pendingChunkTimeout = new HashMap<>();
	// To save vanilla query packet transaction
	private final Map<Integer, Either<BlockPos, Integer>> transactionToBlockPosOrEntityId = new HashMap<>();
	private ClientWorld clientWorld;

	// Backup Chunk Saving task
	private boolean sentBackupPackets = false;
	private boolean receivedBackupPackets = false;
	private final HashMap<ChunkPos, Set<BlockPos>> pendingBackupChunk_BlockEntities = new HashMap<>();

	@Nullable
	public World getWorld()
	{
		return fi.dy.masa.malilib.util.WorldUtils.getBestWorld(mc);
	}

	public ClientWorld getClientWorld()
	{
		if (this.clientWorld == null)
		{
			clientWorld = mc.world;
		}

		return clientWorld;
	}

	private EntitiesDataStorage() { }

	@Override
	public void onClientTick(MinecraftClient mc)
	{
		long now = System.currentTimeMillis();
		//this.uptimeTicks++;

		if (now - this.serverTickTime > 50)
		{
			// Expire cached NBT
			this.tickCache(now);

			// 5 queries / server tick
			for (int i = 0; i < LitematicConfigs.SERVER_NBT_REQUEST_RATE.getIntegerValue(); i++)
			{
				if (!this.pendingBlockEntitiesQueue.isEmpty())
				{
					var iter = this.pendingBlockEntitiesQueue.iterator();
					BlockPos pos = iter.next();
					iter.remove();
					if (this.hasPcaServer())
					{
						requestPcaBlockEntityData(pos);
					}
					else if (this.shouldUseQuery())
					{
						// Only check once if we have OP
						requestQueryBlockEntity(pos);
					}
				}
			}
			this.serverTickTime = System.currentTimeMillis();
		}
	}

	private static ClientPlayNetworkHandler getVanillaHandler()
	{
		if (mc.player != null)
		{
			return mc.player.networkHandler;
		}

		return null;
	}

	public void reset(boolean isLogout)
	{
		if (isLogout)
		{
			Litematica.debugLog("EntitiesDataStorage#reset() - log-out");
			this.sentBackupPackets = false;
			this.receivedBackupPackets = false;
			this.checkOpStatus = false;
			this.hasOpStatus = false;
			this.lastOpCheck = 0L;
		}
		else
		{
			Litematica.debugLog("EntitiesDataStorage#reset() - dimension change or log-in");
			long now = System.currentTimeMillis();
			this.serverTickTime = now - (this.getCacheTimeout() + 5000L);
			this.tickCache(now);
			this.serverTickTime = now;
			this.clientWorld = mc.world;
			this.checkOpStatus = true;
			this.lastOpCheck = now;
		}

		// Clear data
		this.blockEntityCache.clear();
		this.pendingBlockEntitiesQueue.clear();

		// Litematic Save values
		this.completedChunks.clear();
		this.pendingChunks.clear();
		this.pendingChunkTimeout.clear();
		this.pendingBackupChunk_BlockEntities.clear();
	}

	private boolean shouldUseQuery()
	{
		if (this.hasOpStatus) return true;
		if (this.checkOpStatus)
		{
			// Check for 15 minutes after login, or changing dimensions
			if ((System.currentTimeMillis() - this.lastOpCheck) < 900000L) return true;
			this.checkOpStatus = false;
		}

		return false;
	}

	public void resetOpCheck()
	{
		this.hasOpStatus = false;
		this.checkOpStatus = true;
		this.lastOpCheck = System.currentTimeMillis();
	}

	private long getCacheTimeout()
	{
		return (long) (MathHelper.clamp(LitematicConfigs.ENTITY_DATA_SYNC_CACHE_TIMEOUT.getDoubleValue(), 0.25, 30.0) * 1000L);
	}

	private long getCacheTimeoutLong()
	{
		return (long) (MathHelper.clamp((LitematicConfigs.ENTITY_DATA_SYNC_CACHE_TIMEOUT.getDoubleValue() * this.longCacheTimeout), 120.0, 300.0) * 1000L);
	}

	private void tickCache(long nowTime)
	{
		long blockTimeout = this.getCacheTimeout();
		int count;
		boolean beEmpty = false;

		// Use LongTimeouts when saving a Litematic Selection,
		// which is pretty much the standard value x 30 (min 120, max 300 seconds)
		if (this.shouldUseLongTimeout)
		{
			blockTimeout = this.getCacheTimeoutLong();

			// Add extra time if using QueryNbt only
			if (!this.hasPcaServer() && this.getIfReceivedBackupPackets())
			{
				blockTimeout += 3000L;
			}
		}

		synchronized (this.blockEntityCache)
		{
			count = 0;

			for (BlockPos pos : this.blockEntityCache.keySet())
			{
				Pair<Long, Pair<BlockEntity, NbtCompound>> pair = this.blockEntityCache.get(pos);

				if (nowTime - pair.getLeft() > blockTimeout || pair.getLeft() > nowTime)
				{
					//Litematica.debugLog("litematicEntityCache: be at pos [{}] has timed out by [{}] ms", pos.toShortString(), blockTimeout);
					this.blockEntityCache.remove(pos);
				}
				else
				{
					count++;
				}
			}

			if (count == 0)
			{
				beEmpty = true;
			}
		}

		// End Long timeout phase
		if (beEmpty && this.shouldUseLongTimeout)
		{
			this.shouldUseLongTimeout = false;
		}
	}

	public @Nullable NbtCompound getFromBlockEntityCacheNbt(BlockPos pos)
	{
		if (this.blockEntityCache.containsKey(pos))
		{
			return this.blockEntityCache.get(pos).getRight().getRight();
		}

		return null;
	}

	public @Nullable BlockEntity getFromBlockEntityCache(BlockPos pos)
	{
		if (this.blockEntityCache.containsKey(pos))
		{
			return this.blockEntityCache.get(pos).getRight().getLeft();
		}

		return null;
	}

	public boolean hasPcaServer()
	{
		return PcaSyncProtocol.enable;
	}

	public int getPendingBlockEntitiesCount()
	{
		return this.pendingBlockEntitiesQueue.size();
	}

	public int getBlockEntityCacheCount()
	{
		return this.blockEntityCache.size();
	}

	public boolean getIfReceivedBackupPackets()
	{
		return this.sentBackupPackets & this.receivedBackupPackets;
	}

	public @Nullable Pair<BlockEntity, NbtCompound> requestBlockEntity(World world, BlockPos pos)
	{
		// Don't cache/request a BE for the Schematic World
		if (world instanceof WorldSchematic)
		{
			BlockEntity be = world.getWorldChunk(pos).getBlockEntity(pos);

			if (be != null)
			{
				NbtCompound nbt = be.createNbtWithIdentifyingData();

				return Pair.of(be, nbt);
			}
		}
		if (this.blockEntityCache.containsKey(pos))
		{
			// Refresh at 25%
			if (!(world.getServer() instanceof IntegratedServer))
			{
				if (System.currentTimeMillis() - this.blockEntityCache.get(pos).getLeft() > (this.getCacheTimeout() / 4))
				{
					//Litematica.debugLog("requestBlockEntity: be at pos [{}] requeue at [{}] ms", pos.toShortString(), this.getCacheTimeout() / 4);
					this.pendingBlockEntitiesQueue.add(pos);
				}
			}

			return this.blockEntityCache.get(pos).getRight();
		}
		else if (world.getBlockState(pos).getBlock() instanceof BlockEntityProvider)
		{
			if (!(world.getServer() instanceof IntegratedServer))
			{
				this.pendingBlockEntitiesQueue.add(pos);
			}

			BlockEntity be = world.getWorldChunk(pos).getBlockEntity(pos);

			if (be != null)
			{
				NbtCompound nbt = be.createNbtWithIdentifyingData();
				Pair<BlockEntity, NbtCompound> pair = Pair.of(be, nbt);

				synchronized (this.blockEntityCache)
				{
					this.blockEntityCache.put(pos, Pair.of(System.currentTimeMillis(), pair));
				}

				return pair;
			}
		}

		return null;
	}

	@Nullable
	public Inventory getBlockInventory(World world, BlockPos pos, boolean useNbt)
	{
		if (world instanceof WorldSchematic)
		{
			return InventoryUtils.getInventory(world, pos);
		}
		if (this.blockEntityCache.containsKey(pos))
		{
			Inventory inv = null;

			if (useNbt)
			{
				inv = ContainerUtils.getNbtInventory(this.blockEntityCache.get(pos).getRight().getRight(), -1);
			}
			else
			{
				BlockEntity be = this.blockEntityCache.get(pos).getRight().getLeft();
				BlockState state = world.getBlockState(pos);

				if (state.getBlock() instanceof AirBlock || state.equals(Blocks.AIR.getDefaultState()))
				{
					return null;
				}

				if (be instanceof Inventory inv1)
				{
					if (be instanceof ChestBlockEntity)
					{
						ChestType type = state.get(ChestBlock.CHEST_TYPE);

						if (type != ChestType.SINGLE)
						{
							BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
							if (!world.isChunkLoaded(posAdj)) return null;
							BlockState stateAdj = world.getBlockState(posAdj);

							var dataAdj = this.getFromBlockEntityCache(posAdj);

							if (dataAdj == null)
							{
								this.requestBlockEntity(world, posAdj);
							}

							if (stateAdj.getBlock() == state.getBlock() &&
								dataAdj instanceof ChestBlockEntity inv2 &&
								stateAdj.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE &&
								stateAdj.get(ChestBlock.FACING) == state.get(ChestBlock.FACING))
							{
								Inventory invRight = type == ChestType.RIGHT ? inv1 : inv2;
								Inventory invLeft = type == ChestType.RIGHT ? inv2 : inv1;

								inv = new DoubleInventory(invRight, invLeft);
							}
						}
						else
						{
							inv = inv1;
						}
					}
					else
					{
						inv = inv1;
					}
				}
			}

			if (inv != null)
			{
				return inv;
			}
		}

		this.requestBlockEntity(world, pos);

		return null;
	}

	private void requestQueryBlockEntity(BlockPos pos)
	{
		ClientPlayNetworkHandler handler = getVanillaHandler();

		if (handler != null)
		{
			this.sentBackupPackets = true;
			handler.getDataQueryHandler().queryBlockNbt(pos, nbtCompound ->
			{
				handleBlockEntityData(pos, nbtCompound, true);
			});
			this.transactionToBlockPosOrEntityId.put(((DataQueryHandlerAccessor) handler.getDataQueryHandler()).expectedTransactionId(), Either.left(pos));
		}
	}

	private void requestPcaBlockEntityData(BlockPos pos)
	{
		PcaSyncProtocol.syncBlockEntity(pos);
	}

	public void requestBackupBulkEntityData(ChunkPos chunkPos, int minY, int maxY)
	{
		if (!this.getIfReceivedBackupPackets() || !this.hasPcaServer())
		{
			return;
		}

		this.completedChunks.remove(chunkPos);
		minY = MathHelper.clamp(minY, -64, 319);
		maxY = MathHelper.clamp(maxY, -64, 319);

		ClientWorld world = this.getClientWorld();
		Chunk chunk = world != null ? world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) : null;

		if (chunk == null)
		{
			return;
		}

		Set<BlockPos> teSet = chunk.getBlockEntityPositions();

		Litematica.debugLog("EntitiesDataStorage#requestBackupBulkEntityData(): for chunkPos {} (minY [{}], maxY [{}]) // Request --> TE: [{}]", chunkPos.toString(), minY, maxY, teSet.size());
		//System.out.printf("0: ChunkPos [%s], Box [%s] // teSet [%d], entList [%d]\n", chunkPos.toString(), bb.toString(), teSet.size(), entList.size());

		for (BlockPos tePos : teSet)
		{
			if ((tePos.getX() < chunkPos.getStartX() || tePos.getX() > chunkPos.getEndX()) ||
				(tePos.getZ() < chunkPos.getStartZ() || tePos.getZ() > chunkPos.getEndZ()) ||
				(tePos.getY() < minY || tePos.getY() > maxY))
			{
				continue;
			}

			this.requestBlockEntity(world, tePos);
		}

		if (!teSet.isEmpty())
		{
			this.pendingBackupChunk_BlockEntities.put(chunkPos, teSet);
		}

		if (!teSet.isEmpty())
		{
			this.pendingChunks.add(chunkPos);
			this.pendingChunkTimeout.put(chunkPos, Util.getMeasuringTimeMs());
		}
		else
		{
			this.completedChunks.add(chunkPos);
		}
	}

	private boolean markBackupBlockEntityComplete(ChunkPos chunkPos, BlockPos pos)
	{
		if (!this.getIfReceivedBackupPackets())
		{
			return true;
		}

		//Litematica.debugLog("EntitiesDataStorage#markBackupBlockEntityComplete() - Marking ChunkPos {} - Block Entity at [{}] as complete.", chunkPos.toString(), pos.toShortString());

		if (this.pendingChunks.contains(chunkPos))
		{
			if (this.pendingBackupChunk_BlockEntities.containsKey(chunkPos))
			{
				Set<BlockPos> teSet = this.pendingBackupChunk_BlockEntities.get(chunkPos);

				if (teSet.contains(pos))
				{
					teSet.remove(pos);

					if (teSet.isEmpty())
					{
						Litematica.debugLog("EntitiesDataStorage#markBackupBlockEntityComplete(): ChunkPos {} - Block Entity List Complete!", chunkPos.toString());
						this.pendingBackupChunk_BlockEntities.remove(chunkPos);
						this.pendingChunks.remove(chunkPos);
						this.pendingChunkTimeout.remove(chunkPos);
						this.completedChunks.add(chunkPos);
						return true;
					}
					else
					{
						this.pendingBackupChunk_BlockEntities.replace(chunkPos, teSet);
					}
				}
			}
		}

		return false;
	}

	@Nullable
	public BlockEntity handleBlockEntityData(BlockPos pos, NbtCompound nbt, boolean updateBE)
	{
		this.pendingBlockEntitiesQueue.remove(pos);
		if (nbt == null || this.getClientWorld() == null) return null;

		BlockEntity blockEntity = this.getClientWorld().getBlockEntity(pos);

		if (blockEntity != null)
		{
			if (!nbt.contains("id", NbtElement.STRING_TYPE))
			{
				Identifier id = BlockEntityType.getId(blockEntity.getType());

				if (id != null)
				{
					nbt.putString("id", id.toString());
				}
			}

			synchronized (this.blockEntityCache)
			{
				this.blockEntityCache.put(pos, Pair.of(System.currentTimeMillis(), Pair.of(blockEntity, nbt)));
			}

			if (updateBE)
			{
				blockEntity.readNbt(nbt);
			}

			ChunkPos chunkPos = new ChunkPos(pos);

			if (this.hasPendingChunk(chunkPos))
			{
				this.markBackupBlockEntityComplete(chunkPos, pos);
			}

			return blockEntity;
		}

		return null;
	}

	public void handleVanillaQueryNbt(int transactionId, NbtCompound nbt)
	{
		if (this.checkOpStatus)
		{
			this.hasOpStatus = true;
			this.checkOpStatus = false;
			this.lastOpCheck = System.currentTimeMillis();
		}

		Either<BlockPos, Integer> either = this.transactionToBlockPosOrEntityId.remove(transactionId);

		if (either != null)
		{
			this.receivedBackupPackets = true;
			either.ifLeft(pos -> handleBlockEntityData(pos, nbt, true));
		}
	}

	public boolean hasPendingChunk(ChunkPos pos)
	{
		if (this.hasPcaServer() || this.getIfReceivedBackupPackets())
		{
			return this.pendingChunks.contains(pos);
		}

		return false;
	}

	private void checkForPendingChunkTimeout(ChunkPos pos)
	{
		if ((this.hasPcaServer() && this.hasPendingChunk(pos)) ||
			(this.getIfReceivedBackupPackets() && this.hasPendingChunk(pos)))
		{
			long now = Util.getMeasuringTimeMs();

			// Take no action when ChunkPos is not loaded by the ClientWorld.
			if (!WorldUtils.isClientChunkLoaded(mc.world, pos.x, pos.z))
			{
				this.pendingChunkTimeout.replace(pos, now);
				return;
			}

			long duration = now - this.pendingChunkTimeout.get(pos);

			if (duration > (this.getChunkTimeoutMs()))
			{
				Litematica.debugLog("EntitiesDataStorage#checkForPendingChunkTimeout(): [ChunkPos {}] has timed out waiting for data, marking complete without Receiving Entity Data.", pos.toString());
				this.pendingChunkTimeout.remove(pos);
				this.pendingChunks.remove(pos);
				this.completedChunks.add(pos);
			}
		}
	}

	private long getChunkTimeoutMs()
	{
		if (this.hasPcaServer())
		{
			return this.chunkTimeoutMs;
		}
		else if (this.getIfReceivedBackupPackets())
		{
			return this.chunkTimeoutMs + 3000L;
		}

		return 1000L;
	}

	public boolean hasCompletedChunk(ChunkPos pos)
	{
		if (this.hasPcaServer() || this.getIfReceivedBackupPackets())
		{
			this.checkForPendingChunkTimeout(pos);
			return this.completedChunks.contains(pos);
		}

		return true;
	}

	public void markCompletedChunkDirty(ChunkPos pos)
	{
		if (this.hasPcaServer() || this.getIfReceivedBackupPackets())
		{
			this.completedChunks.remove(pos);
		}
	}
}
