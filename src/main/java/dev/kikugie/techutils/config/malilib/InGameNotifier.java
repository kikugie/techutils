package dev.kikugie.techutils.config.malilib;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.render.MessageRenderer;

public class InGameNotifier implements IMessageConsumer {
    public static final InGameNotifier INSTANCE = new InGameNotifier();
    private final MessageRenderer messageRenderer = new MessageRenderer(0xDD000000, 0xFF999999);

    @Override
    public void addMessage(Message.MessageType type, String messageKey, Object... args) {
        this.messageRenderer.addMessage(type, 2000, messageKey, args);
    }

    @Override
    public void addMessage(Message.MessageType type, int lifeTime, String messageKey, Object... args) {
        this.messageRenderer.addMessage(type, lifeTime, messageKey, args);
    }
}
