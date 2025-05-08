package dev.kikugie.techutils.feature.worldedit;

import dev.kikugie.techutils.TechUtilsMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Opens a WorldEdit CUI channel and passes packets to {@link WorldEditSync} instance.
 * <p>
 * If channel has already been registered by another mod, it leeches packets from it instead.
 */
@SuppressWarnings("UnstableApiUsage")
public class WorldEditNetworkHandler {
	public static final Identifier CHANNEL = new Identifier("worldedit", "cui");
	private static final int PROTOCOL = 4;
	private static WorldEditNetworkHandler instance;
	public final WorldEditStorage storage;
	private boolean worldEditConnected = false;
	private boolean yoinkPackets = false;

	private WorldEditNetworkHandler() {
		this.storage = WorldEditStorage.init();
		var receivers = ClientPlayNetworking.getGlobalReceivers();
		if (receivers.contains(CHANNEL)) {
			this.yoinkPackets = true;
			return;
		}
		ClientPlayNetworking.registerReceiver(CHANNEL, this::onPacket);
		handshake();
	}

	public static WorldEditNetworkHandler initHandler() {
		if (MinecraftClient.getInstance().world == null) {
			throw new RuntimeException("Registering WorldEdit handler in non-world context!");
		}
		instance = new WorldEditNetworkHandler();
		return instance;
	}

	public static Optional<WorldEditNetworkHandler> getInstance() {
		return Optional.ofNullable(instance);
	}

	public void onYoinkedPacket(PacketByteBuf packet) {
		if (!this.yoinkPackets) return;
		onPacket(MinecraftClient.getInstance(), MinecraftClient.getInstance().getNetworkHandler(), packet, ClientPlayNetworking.getSender());
	}

	private void onPacket(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf data, PacketSender packetSender) {
		if (!this.worldEditConnected) {
			this.worldEditConnected = true;
			TechUtilsMod.LOGGER.info("WorldEdit connected");
			WorldEditSync.getInstance().ifPresent(WorldEditSync::onWorldEditConnected);
		}
		int bytes = data.readableBytes();
		if (bytes == 0) {
			TechUtilsMod.LOGGER.warn("WorldEditNetworkHandler#onPacket(): Received CUI packet of length zero");
			return;
		}
		String message = data.toString(0, data.readableBytes(), StandardCharsets.UTF_8);
		String[] split = message.split("\\|", -1);
		boolean multi = split[0].startsWith("+");
		String type = split[0].substring(multi ? 1 : 0);
		String[] args = message.substring(type.length() + (multi ? 2 : 1)).split("\\|", -1);
		TechUtilsMod.LOGGER.info(message);
		handlePacket(type, args);
	}

	private void handlePacket(String type, String[] args) {
		if (type.equals("s")) {
			this.storage.setCuboid(args[0].equals("cuboid"));
			return;
		}
		if (!type.equals("p") || !this.storage.isCuboid())
			return;
		try {
			int p = Integer.parseInt(args[0]);
			BlockPos pos = new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			this.storage.setPos(p == 0, pos);
		} catch (NumberFormatException e) {
			TechUtilsMod.LOGGER.warn("WorldEditCUINetworkHandler#handlePacket(): Failed int parsing of position");
			e.printStackTrace();
		}
	}

	private void handshake() {
		String message = "v|" + PROTOCOL;
		ByteBuf buf = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
		ClientPlayNetworking.send(CHANNEL, new PacketByteBuf(buf));
	}
}
