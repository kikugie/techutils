package dev.kikugie.techutils.mixin.mod.litematica;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces Litematica's default method of selecting custom preview image with a file selection menu.
 */
@Mixin(targets = "fi/dy/masa/litematica/gui/GuiSchematicManager$ButtonListener", remap = false)
public class GuiSchematicManagerMixin {
    private static final String[] fileFormats = {"jpg", "png", "bmp"};

    /**
     * Dark pointer magic taken from LWJGUI.
     *
     * @see <a href="https://github.com/orange451/LWJGUI/blob/bdc10971be84157e05aa0dbc1eccb6e51c5b04ca/src/main/java/lwjgui/LWJGUIDialog.java#L85">Source</a>
     */
    @ModifyArg(method = "actionPerformedWithButton",
            at = @At(value = "INVOKE", target = "Ljava/io/File;<init>(Ljava/io/File;Ljava/lang/String;)V"))
    private String pickCustomImage(String value) {
        PointerBuffer filters;
        String selectedFile;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            filters = stack.mallocPointer(fileFormats.length);
            for (String format : fileFormats) {
                filters.put(stack.UTF8("*." + format));
            }
            filters.flip();
            selectedFile = TinyFileDialogs.tinyfd_openFileDialog(
                    "Select a preview image",
                    value.replace("thumb.png", ""),
                    filters,
                    "Image files",
                    false
            );
            stack.pop();
        }

        if (selectedFile == null) {
            InfoUtils.showGuiAndInGameMessage(Message.MessageType.ERROR, "Image not selected");
            return value;
        }
        return selectedFile;
    }

    @Redirect(method = "actionPerformedWithButton",
            at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/GuiBase;isShiftDown()Z"))
    private boolean dontRequireShift() {
        return true;
    }

    @Redirect(method = "actionPerformedWithButton",
            at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/GuiBase;isAltDown()Z"))
    private boolean dontRequireAlt() {
        return true;
    }

    @ModifyArg(method = "actionPerformedWithButton",
            at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/util/InfoUtils;showGuiAndInGameMessage(Lfi/dy/masa/malilib/gui/Message$MessageType;Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 2))
    // FIXME: Still doesn't remove the message
    private String muteOriginalError(String translationKey) {
        return "";
    }
}
