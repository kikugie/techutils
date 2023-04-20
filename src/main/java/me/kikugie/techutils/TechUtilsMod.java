package me.kikugie.techutils;

import fi.dy.masa.malilib.event.InitializationHandler;
import me.kikugie.techutils.command.IsorenderSelectionCommand;
import me.kikugie.techutils.feature.WorldEditActionManager;
import me.kikugie.techutils.feature.inverifier.ContainerStorage;
import me.kikugie.techutils.feature.inverifier.VerifierRecorder;
import me.kikugie.techutils.networking.GamerQueryHandler;
import me.kikugie.techutils.utils.ResponseMuffler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TechUtilsMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    @Override
    public void onInitializeClient() {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
        ClientCommandRegistrationCallback.EVENT.register(IsorenderSelectionCommand::register);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorldEditActionManager.initManager());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            WorldEditActionManager.reset();
            ResponseMuffler.clear();
        });
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            GamerQueryHandler.INSTANCE.onTick();
        });
    }
}