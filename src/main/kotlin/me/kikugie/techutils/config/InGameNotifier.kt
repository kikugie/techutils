package me.kikugie.techutils.config

import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer
import fi.dy.masa.malilib.render.MessageRenderer

class InGameNotifier : IMessageConsumer {
    override fun addMessage(type: Message.MessageType, messageKey: String, vararg args: Any) {
        messageRenderer.addMessage(type, 2000, messageKey, *args)
    }

    override fun addMessage(type: Message.MessageType, lifeTime: Int, messageKey: String, vararg args: Any) {
        messageRenderer.addMessage(type, lifeTime, messageKey, *args)
    }

    companion object {
        val INSTANCE = InGameNotifier()
        private val messageRenderer = MessageRenderer(-0x23000000, -0x666667)
    }
}