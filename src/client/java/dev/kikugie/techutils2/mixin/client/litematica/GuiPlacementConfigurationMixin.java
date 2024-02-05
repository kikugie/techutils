package dev.kikugie.techutils2.mixin.client.litematica;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.kikugie.techutils2.gui.ImporterGui;
import dev.kikugie.techutils2.gui.util.StaticButton;
import dev.kikugie.techutils2.gui.icon.GuiIcon;
import dev.kikugie.techutils2.impl.serializer.PlacementSerializer;
import dev.kikugie.techutils2.util.TextUtils;
import fi.dy.masa.litematica.gui.GuiPlacementConfiguration;
import fi.dy.masa.litematica.gui.widgets.WidgetListPlacementSubRegions;
import fi.dy.masa.litematica.gui.widgets.WidgetPlacementSubRegion;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.StringUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * // dev info<br>
 * ## Translations
 * <ul>
 *  <li>{@code techutils.placement.export_button}</li>
 *  <li>{@code techutils.placement.import_button}</li>
 *  <li>{@code techutils.placement.export_fail}</li>
 *  <li>{@code techutils.placement.export_success}</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
@Mixin(value = GuiPlacementConfiguration.class, remap = false)
abstract class GuiPlacementConfigurationMixin extends GuiListBase<SubRegionPlacement, WidgetPlacementSubRegion, WidgetListPlacementSubRegions> {

    @Shadow
    @Final
    public SchematicPlacement placement;

    protected GuiPlacementConfigurationMixin(int listX, int listY) {
        super(listX, listY);
    }

    @ModifyArg(method = "initGui", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;<init>(IIIILjava/lang/String;[Ljava/lang/String;)V", ordinal = 1), index = 2)
    private int fixBackButtonWidth(int width) {
        return Math.max(width, 120);
    }

    @ModifyArg(method = "initGui", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;<init>(IIIILjava/lang/String;[Ljava/lang/String;)V", ordinal = 1), index = 0)
    private int fixBackButtonX(int x) {
        return x - 4;
    }

    @ModifyExpressionValue(method = "initGui", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int reserveSpaceForSerializer(int original, @Share("width") LocalIntRef width) {
        width.set(original);
        return original - (20 * 2 + 4);
    }

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/util/StringUtils;translate(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", ordinal = 0))
    private void createSerializerButtons(CallbackInfo ci, @Share("width") LocalIntRef width, @Local(ordinal = 2) int x, @Local(ordinal = 3) int y) {
        int bx = x + width.get() - TextUtils.INSTANCE.getTextRenderer().getWidth(StringUtils.translate("litematica.gui.button.rename")) + (20 * 2 + 4);
        addButton(new StaticButton(bx, y, 20, 20, "", GuiIcon.Companion.getEXPORT(), StringUtils.translate("techutils.placement.export_button")),
                (button, key) -> showExportMessage(key));
        addButton(new StaticButton(bx + 22, y, 20, 20, "", GuiIcon.Companion.getIMPORT(), StringUtils.translate("techutils.placement.import_button")),
                (button, key) -> openImportGui(key));
    }

    @Unique
    private void showExportMessage(int key) {
        if (key != 0) return;
        String value = PlacementSerializer.INSTANCE.serialize(this.placement);
        if (value == null) {
            addMessage(Message.MessageType.ERROR, "techutils.placement.export_fail");
        } else {
            TextUtils.INSTANCE.setClipboard(value);
            addMessage(Message.MessageType.SUCCESS, "techutils.placement.export_success", value);
        }
    }

    @Unique
    private void openImportGui(int key) {
        if (key == 0) openGui(new ImporterGui(this.placement));
    }
}
