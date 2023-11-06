package dev.kikugie.techutils.client.util.multiversion

import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener
import fi.dy.masa.malilib.gui.widgets.WidgetBase
import fi.dy.masa.malilib.gui.widgets.WidgetContainer

/**
 * Wrapper for change in onMouseScrolled
 */
interface Interactable {
    fun onScrolled(x: Int, y: Int, amount: Double): Boolean
    fun onClicked(x: Int, y: Int, button: Int): Boolean
    fun onReleased(x: Int, y: Int, button: Int): Boolean
    fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int): Boolean
}

abstract class WidgetSchematicBrowserExtension(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    parent: GuiSchematicBrowserBase,
    selectionListener: ISelectionListener<DirectoryEntry>? = null
) : WidgetSchematicBrowser(x, y, width, height, parent, selectionListener), Interactable {
    override fun onMouseScrolled(mouseX: Int, mouseY: Int, verticalAmount: Double) =
        onScrolled(mouseX, mouseY, verticalAmount) || super.onMouseScrolled(
            mouseX,
            mouseY,
            verticalAmount
        )

    override fun onMouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) =
        onReleased(mouseX, mouseY, mouseButton) || super.onMouseReleased(mouseX, mouseY, mouseButton)

    override fun mouseDragged(mouseX: Double, mouseY: Double, mouseButton: Int, deltaX: Double, deltaY: Double) =
        onDragged(mouseX, mouseY, deltaX, deltaY, mouseButton) || super.mouseDragged(
            mouseX,
            mouseY,
            mouseButton,
            deltaX,
            deltaY
        )

    override fun onClicked(x: Int, y: Int, button: Int) = onMouseClicked(x, y, button)
}

abstract class MetadataWidgetExtension : WidgetContainer(0, 0, 0, 0), Interactable {
    override fun onMouseScrolledImpl(
        mouseX: Int,
        mouseY: Int,
        verticalAmount: Double
    ) = onScrolled(mouseX, mouseY, verticalAmount) || super.onMouseScrolledImpl(
        mouseX,
        mouseY,
        verticalAmount
    )

    override fun onClicked(x: Int, y: Int, button: Int) = onMouseClicked(x, y, button)

    override fun onReleased(x: Int, y: Int, button: Int): Boolean {
        onMouseReleasedImpl(x, y, button)
        return true
    }
}

abstract class ModelWidgetExtension : WidgetBase(0, 0, 0, 0), Interactable {
    override fun onMouseScrolledImpl(
        mouseX: Int,
        mouseY: Int,
        verticalAmount: Double
    ) = onScrolled(mouseX, mouseY, verticalAmount)

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) = onClicked(mouseX, mouseY, mouseButton)

    override fun onMouseReleasedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) {
        onReleased(mouseX, mouseY, mouseButton)
    }
}