package me.kikugie.techutils

import fi.dy.masa.malilib.event.InitializationHandler
import me.kikugie.techutils.feature.WorldEditActionManager
import me.kikugie.techutils.utils.ResponseMuffler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TechUtilsMod : ModInitializer {
    override fun onInitialize() {
        InitializationHandler.getInstance().registerInitializationHandler(InitHandler())
        ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { _: ClientPlayNetworkHandler?, _: PacketSender?, _: MinecraftClient? -> WorldEditActionManager.init() })
        ClientPlayConnectionEvents.DISCONNECT.register(ClientPlayConnectionEvents.Disconnect { _: ClientPlayNetworkHandler?, _: MinecraftClient? ->
            WorldEditActionManager.reset()
            ResponseMuffler.clear()
        })
    }

    companion object {
        @JvmField
        val LOGGER: Logger = LoggerFactory.getLogger("tech-utils")
    }
}