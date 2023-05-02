package me.kikugie.techutils.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.kikugie.techutils.TechUtilsMod;
import me.kikugie.techutils.feature.worldedit.WorldEditActionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;

public class WorldEditNetworkHandler {
    public static final Identifier CHANNEL = new Identifier("worldedit:cui");
    private static final int PROTOCOL = 4;
    private static WorldEditNetworkHandler instance;
    public final WorldEditStorage storage;
    private boolean worldEditConnected = false;
    private boolean yoinkPacketsFromExistingChannel = false;

    private WorldEditNetworkHandler() {
        this.storage = new WorldEditStorage();
        var receivers = ClientPlayNetworking.getGlobalReceivers();
        if (receivers.contains(CHANNEL)) {
            yoinkPacketsFromExistingChannel = true;
            return;
        }
        ClientPlayNetworking.registerReceiver(CHANNEL, this::onPacket);
        handshake();
    }

    public static WorldEditNetworkHandler initHandler() {
        if (MinecraftClient.getInstance().world == null) {
            throw new RuntimeException("Registering handler in non-world context!");
        }
        instance = new WorldEditNetworkHandler();
        return instance;
    }

    public static WorldEditNetworkHandler getInstance() {
        return instance;
    }

    public void onYoinkedPacket(CustomPayloadS2CPacket packet) {
        if (!yoinkPacketsFromExistingChannel) return;
        onPacket(MinecraftClient.getInstance(), MinecraftClient.getInstance().getNetworkHandler(), packet.getData(), ClientPlayNetworking.getSender());
    }

    private void onPacket(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf data, PacketSender packetSender) {
        if (!worldEditConnected) {
            worldEditConnected = true;
            TechUtilsMod.LOGGER.info("WorldEdit connected");
            WorldEditActionManager.getInstance().onWorldEditConnected();
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
            storage.parseMode(args[0]);
            return;
        }
        if (!type.equals("p") || !storage.isCuboidMode()) return;
        try {
            int p = Integer.parseInt(args[0]);
            BlockPos pos = new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            storage.setPos(p, pos);
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
