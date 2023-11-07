package dev.kikugie.techutils.client.util.litematica

import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object InGameNotifier : IMessageConsumer {
    val client: MinecraftClient
        get() = MinecraftClient.getInstance()

    override fun addMessage(type: Message.MessageType, key: String, vararg args: Any?) {
        client.player?.sendMessage(Text.of("${type.formatting}${StringUtils.translate(key, args)}"), true)
    }

    override fun addMessage(type: Message.MessageType, lifeTime: Int, key: String, vararg args: Any?) {
        addMessage(type, key, args)
    }
}