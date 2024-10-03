package dev.kikugie.techutils.feature.worldedit;

import com.sk89q.worldedit.fabric.net.handler.WECUIPacketHandler;
import dev.kikugie.techutils.TechUtilsMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Opens a WorldEdit CUI channel and passes packets to {@link WorldEditSync} instance.
 * <p>
 * If channel has already been registered by another mod, it leeches packets from it instead.
 */
public class WorldEditNetworkHandler {
	public static final Identifier CHANNEL = Identifier.of("worldedit", "cui");
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
		ClientPlayNetworking.registerReceiver(WECUIPacketHandler.CuiPacket.TYPE, (payload, context) -> this.onPacket(payload.text()));
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

	public void onYoinkedPacket(CustomPayload payload) {
		if (!this.yoinkPackets) return;
		onPacket(((WECUIPacketHandler.CuiPacket) payload).text());
	}

	private void onPacket(String message) {
		if (!this.worldEditConnected) {
			this.worldEditConnected = true;
			TechUtilsMod.LOGGER.info("WorldEdit connected");
			WorldEditSync.getInstance().ifPresent(WorldEditSync::onWorldEditConnected);
		}
		if (message.isEmpty()) {
			TechUtilsMod.LOGGER.warn("WorldEditNetworkHandler#onPacket(): Received CUI packet of length zero");
			return;
		}
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
		ClientPlayNetworking.send(new WECUIPacketHandler.CuiPacket(message));
	}
}
