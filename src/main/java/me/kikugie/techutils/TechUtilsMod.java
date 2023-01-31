package me.kikugie.techutils;

import fi.dy.masa.malilib.event.InitializationHandler;
import me.kikugie.techutils.command.IsorenderSelectionCommand;
import me.kikugie.techutils.feature.WorldEditActionManager;
import me.kikugie.techutils.utils.ResponseMuffler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechUtilsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    @Override
    public void onInitialize() {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
        ClientCommandRegistrationCallback.EVENT.register(IsorenderSelectionCommand::register);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorldEditActionManager.initManager());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            WorldEditActionManager.reset();
            ResponseMuffler.clear();
        });
    }
}