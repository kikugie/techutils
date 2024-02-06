package dev.kikugie.techutils

import dev.kikugie.malilib_extras.api.config.ConfigRegistry
import dev.kikugie.malilib_extras.api.config.MalilibConfig
import dev.kikugie.techutils.config.*
import dev.kikugie.techutils.gui.ConfigGui
import dev.kikugie.techutils.impl.IsorenderSelectionCommand
import dev.kikugie.techutils.impl.worldedit.WorldEditNetworkHandler
import dev.kikugie.techutils.impl.worldedit.WorldEditSync
import dev.kikugie.techutils.util.isLoaded
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.slf4j.LoggerFactory

object TechUtilsClient : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Tech Utils")
    lateinit var config: MalilibConfig
        private set

    override fun onInitializeClient() {
        config = MalilibConfig.create(Reference.MOD_ID, Reference.VERSION) {
            register(BrowserConfig)
            register(LitematicaMisc)
            register(GeneralMisc)
            register(WorldEditConfig)
        }
        ConfigRegistry.register(config, modmenu = true) { ConfigGui(config, it) }

        if (isLoaded("isometric-renders")) IsorenderSelectionCommand.register()
        registerWorldEditSync()
    }

    private fun registerWorldEditSync() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> WorldEditNetworkHandler.init() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> WorldEditNetworkHandler.close(); WorldEditSync.queue.clear() }
        ClientTickEvents.START_CLIENT_TICK.register { _ -> WorldEditSync.tick(); WorldEditNetworkHandler.INSTANCE?.tick() }
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ -> !WorldEditSync.queue.test(message.string) }
    }
}