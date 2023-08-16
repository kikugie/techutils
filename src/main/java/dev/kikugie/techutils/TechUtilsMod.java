package dev.kikugie.techutils;

import dev.kikugie.techutils.command.IsorenderSelectionCommand;
import dev.kikugie.techutils.config.malilib.InitHandler;
import dev.kikugie.techutils.feature.containerscan.handlers.InteractionHandler;
import dev.kikugie.techutils.feature.containerscan.scanners.ScannerManager;
import dev.kikugie.techutils.feature.worldedit.WorldEditSync;
import dev.kikugie.techutils.render.outline.OutlineRenderer;
import dev.kikugie.techutils.util.ResponseMuffler;
import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechUtilsMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    @Override
    public void onInitializeClient() {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());

        registerCommands();
        registerWorldEditSync();

        ClientTickEvents.START_WORLD_TICK.register(world -> InteractionHandler.tick(world.getTime()));
        ClientTickEvents.START_WORLD_TICK.register(world -> ScannerManager.tick());
        WorldRenderEvents.END.register(OutlineRenderer::render);
//        WorldRenderEvents.END.register(Remderer::onRender);
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(IsorenderSelectionCommand::register);
    }

    private void registerWorldEditSync() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorldEditSync.init());
        ClientTickEvents.START_WORLD_TICK.register(tick -> WorldEditSync.getInstance().ifPresent(WorldEditSync::onTick));
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> !ResponseMuffler.test(message.getString()));
    }

}
