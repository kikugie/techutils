package me.kikugie.techutils.networking;

import me.kikugie.techutils.config.Configs;
import me.kikugie.techutils.config.Configs.LitematicConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Advanced version of {@link net.minecraft.client.network.DataQueryHandler} that provides support for multi-response handling and async requests. Currently only supports block nbt queries.
 *
 * @see Configs.LitematicConfigs#PACKET_RATE
 * @see Configs.LitematicConfigs#PACKET_TIMEOUT
 */
public class GamerQueryHandler {
    public static final GamerQueryHandler INSTANCE = new GamerQueryHandler();

    private final Map<Integer, CompletableFuture<@Nullable NbtCompound>> pendingRequests = new ConcurrentHashMap<>();
    private final Queue<Packet> queue = new ConcurrentLinkedQueue<>();
    private int id = -1;
    private double packetBuffer = 0;

    /**
     * Requests nbt for given array of positions returning completable future of map position to response.
     * <p>
     * - Pending requests are put in {@link GamerQueryHandler#queue}, which processes elements at {@link LitematicConfigs#PACKET_RATE} per client tick.
     * <p>
     * - Each request has {@link LitematicConfigs#PACKET_TIMEOUT} that can be applied up to 3 time. (TODO: make it configurable?)
     * <p>
     * - {@link Configs.LitematicConfigs#QUERY_TIMEOUT} can be applied to returned future to set timeout.
     *
     * @param positions {@link BlockPos}[].
     * @return {@link CompletableFuture}<{@link Map}<{@link BlockPos}, {@link NbtCompound}>>.
     */
    @SuppressWarnings("unchecked")
    public static synchronized CompletableFuture<Map<BlockPos, @Nullable NbtCompound>> queryBlocks(BlockPos[] positions) {
        final int len = positions.length;
        Packet[] packets = new Packet[len];
        CompletableFuture<@Nullable NbtCompound>[] futures = new CompletableFuture[len];

        for (int i = 0; i < len; i++) {
            final int id = INSTANCE.nextId();
            Packet packet = new Packet(id, positions[i]);
            CompletableFuture<@Nullable NbtCompound> future = new CompletableFuture<>();
            INSTANCE.setTimeout(futures, future, packet, i, LitematicConfigs.PACKET_TIMEOUT.getIntegerValue(), 3);

            futures[i] = future;
            packets[i] = packet;
            INSTANCE.pendingRequests.put(id, future);
            INSTANCE.queue.offer(packet);
        }

        return CompletableFuture.allOf(futures).thenApplyAsync(__ -> {
            Map<BlockPos, @Nullable NbtCompound> result = new ConcurrentHashMap<>();
            for (int i = 0; i < len; i++) {
                result.put(positions[i], futures[i].join());
            }
            return result;
        });
    }

    public void handleQueryResponse(int id, @Nullable NbtCompound nbt) {
        CompletableFuture<NbtCompound> future = pendingRequests.remove(id);
        if (future != null) future.complete(nbt);
    }

    private void setTimeout(CompletableFuture<@Nullable NbtCompound>[] collection, CompletableFuture<@Nullable NbtCompound> future, Packet packet, int index, int timeout, int attempts) {
        future.orTimeout(timeout, TimeUnit.MILLISECONDS).whenComplete((result, throwable) -> {
            if (throwable == null || attempts <= 0 || !pendingRequests.containsKey(packet.id)) return;
            CompletableFuture<@Nullable NbtCompound> newFuture = new CompletableFuture<>();
            setTimeout(collection, newFuture, packet, index, timeout, attempts - 1);

            collection[index] = newFuture;
            pendingRequests.put(packet.id, newFuture);
            queue.offer(packet);
        });
    }

    public void onTick() {
        if (queue.isEmpty() || MinecraftClient.getInstance().getNetworkHandler() == null) return;
        packetBuffer += LitematicConfigs.PACKET_RATE.getDoubleValue();
        int availablePackets = (int) packetBuffer;
        packetBuffer -= availablePackets;
        for (int i = 0; i < Math.min(queue.size(), availablePackets); i++) {
            Packet packet = queue.remove();
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new QueryBlockNbtC2SPacket(packet.id, packet.pos));
        }
    }

    private int nextId() {
        return id < 0 ? id-- : -1;
    }

    public int getRemainingPackets() {
        return pendingRequests.size();
    }

    record Packet(int id, BlockPos pos) {
    }
}
