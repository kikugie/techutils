package dev.kikugie.techutils.impl.worldedit

import com.mojang.brigadier.CommandDispatcher
import dev.kikugie.techutils.TechUtilsClient
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandSource
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.nio.charset.StandardCharsets


class WorldEditNetworkHandler {
    val storage = WorldEditStorage()
    var connected = false
    var available = false
    var yoinkPackets = false
    var initialized = false

    val shouldTick
        get() = connected && available

    init {
        requireNotNull(MinecraftClient.getInstance().world) {
            "Registering WorldEdit handler in non-world context"
        }
        if (CHANNEL in ClientPlayNetworking.getGlobalReceivers())
            yoinkPackets = true
        else {
            ClientPlayNetworking.registerReceiver(CHANNEL) { _, _, it, _ -> onDataPacket(it) }
            handshake()
        }
    }

    fun tick() {
        if (initialized) return
        if (shouldTick) WorldEditSync.onConnected()
        initialized = shouldTick
    }

    fun onCommandTreePacket(dispatcher: CommandDispatcher<CommandSource>) {
        available = dispatcher.findNode(listOf("/pos1")) != null
    }

    fun onDataPacket(data: PacketByteBuf) {
        if (!connected) {
            TechUtilsClient.LOGGER.info("WorldEdit connected!")
            connected = true
        }

        val bytes = data.readableBytes()
        if (bytes == 0) {
            TechUtilsClient.LOGGER.warn("Received CUI packet of length zero")
            return
        }
        val message = data.toString(0, data.readableBytes(), StandardCharsets.UTF_8)
        val split = message.split('|')
        val multi = split[0].startsWith("+")
        val type = split[0].substring(if (multi) 1 else 0)
        val args = message.substring(type.length + (if (multi) 2 else 1)).split('|').toTypedArray()
        TechUtilsClient.LOGGER.debug(message)
        handlePacket(type, *args)
    }

    private fun handlePacket(type: String, vararg args: String) {
        if (args.isEmpty())
            TechUtilsClient.LOGGER.info("Received CUI packet with no arguments")
        else if (type == "s")
            storage.cuboid = args.first() == "cuboid"
        else if (type == "p" && storage.cuboid) try {
            val index = args[0].toInt()
            val pos = BlockPos(args[1].toInt(), args[2].toInt(), args[3].toInt())
            if (index == 0) storage.pos1 = pos
            else storage.pos2 = pos
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn("Failed to parse CUI args [${args.joinToString(", ")}], type $type", e)
        }
    }

    private fun handshake() {
        val message = "v|$PROTOCOL"
        val buf = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8)
        ClientPlayNetworking.send(CHANNEL, PacketByteBuf(buf))
    }

    companion object {
        val CHANNEL = Identifier("worldedit", "cui")
        val PROTOCOL = 4

        var INSTANCE: WorldEditNetworkHandler? = null
            private set

        fun init() {
            INSTANCE = WorldEditNetworkHandler()
        }

        fun close() {
            INSTANCE = null
        }
    }
}