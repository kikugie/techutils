package dev.kikugie.techutils.impl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.kikugie.malilib_extras.util.translate
import dev.kikugie.techutils.util.PlacementUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.text.Text

object IsorenderSelectionCommand {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register(::register)
    }

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>, access: CommandRegistryAccess) {
        dispatcher.register(literal("isorender").then(literal("selection").executes(::renderSelection)))
    }

    private fun renderSelection(context: CommandContext<FabricClientCommandSource>): Int {
        val box = PlacementUtils.selection?.selectedSubRegionBox
        if (box == null || box.pos1 == null || box.pos2 == null)
            context.source.sendError(Text.of("techutils.selection_render.no_selection".translate()))
        else MinecraftClient.getInstance().networkHandler
            ?.sendCommand("isorender area ${box.pos1!!.x} ${box.pos1!!.y} ${box.pos1!!.z} ${box.pos2!!.x} ${box.pos2!!.y} ${box.pos2!!.z}")
        return 1
    }
}