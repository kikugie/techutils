package dev.kikugie.techutils.client.util.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer

object TextUtils {
    val textRenderer: TextRenderer
        get() = MinecraftClient.getInstance().textRenderer

    fun trimFancy(text: String, width: Int): String {
        if (isWithin(text, width)) return text

        val dotWidth = textRenderer.getWidth("...")
        if (dotWidth > width) return ""

        return "${textRenderer.trimToWidth(text, width - dotWidth)}..."
    }

    fun isWithin(text: String, width: Int): Boolean {
        return textRenderer.getWidth(text) <= width
    }
}
