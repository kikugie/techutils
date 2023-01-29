package me.kikugie.techutils.networking

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.kikugie.techutils.TechUtilsMod
import me.kikugie.techutils.feature.WorldEditActionManager
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.nio.charset.StandardCharsets

class WorldEditNetworkHandler {
    val storage: WorldEditStorage = WorldEditStorage()
    private var worldEditConnected = false
    private var yoinkPacketsFromExistingChannel = false

    init {
        val receivers: Set<Identifier> = ClientPlayNetworking.getGlobalReceivers()
        if (receivers.contains(CHANNEL)) {
            yoinkPacketsFromExistingChannel = true
        } else {
            ClientPlayNetworking.registerReceiver(
                CHANNEL
            ) { _: MinecraftClient, _: ClientPlayNetworkHandler?, data: PacketByteBuf, _: PacketSender -> onPacket(data) }
            handshake()
        }
    }

    fun onYoinkedPacket(packet: CustomPayloadS2CPacket) {
        if (!yoinkPacketsFromExistingChannel) return
        onPacket(
            packet.data
        )
    }

    private fun onPacket(
        data: PacketByteBuf
    ) {
        if (!worldEditConnected) {
            worldEditConnected = true
            TechUtilsMod.LOGGER.info("WorldEdit connected")
            WorldEditActionManager.getInstance()?.onWorldEditConnected()
        }
        val bytes: Int = data.readableBytes()
        if (bytes == 0) {
            TechUtilsMod.LOGGER.warn("WorldEditNetworkHandler#onPacket(): Received CUI packet of length zero")
            return
        }
        val message: String = data.toString(0, data.readableBytes(), StandardCharsets.UTF_8)
        val split = message.split(regex = "\\|".toRegex()).toTypedArray()
        val multi = split[0].startsWith("+")
        val type = split[0].substring(if (multi) 1 else 0)
        val args = message.substring(type.length + if (multi) 2 else 1).split("\\|".toRegex()).toTypedArray()
        TechUtilsMod.LOGGER.info(message)
        handlePacket(type, args)
    }

    private fun handlePacket(type: String, args: Array<String>) {
        if (type == "s") {
            storage.parseMode(args[0])
            return
        }
        if (type != "p" || !storage.isCuboidMode) {
            return
        }
        try {
            val p = args[0].toInt()
            val pos = BlockPos(args[1].toInt(), args[2].toInt(), args[3].toInt())
            storage.setPos(p, pos)
        } catch (e: NumberFormatException) {
            TechUtilsMod.LOGGER.warn("WorldEditCUINetworkHandler#handlePacket(): Failed int parsing of position")
            e.printStackTrace()
        }
    }

    private fun handshake() {
        val message = "v|$PROTOCOL"
        val buf: ByteBuf = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8)
        ClientPlayNetworking.send(CHANNEL, PacketByteBuf(buf))
    }

    companion object {
        @JvmStatic
        val CHANNEL = Identifier("worldedit:cui")
        private const val PROTOCOL = 4
        private var instance: WorldEditNetworkHandler? = null

        fun init(): WorldEditNetworkHandler? {
            if (MinecraftClient.getInstance().world == null) {
                throw RuntimeException("Registering handler in non-world context!")
            }
            instance = WorldEditNetworkHandler()
            return instance
        }

        @JvmStatic
        fun getInstance(): WorldEditNetworkHandler? {
            return instance
        }
    }
}