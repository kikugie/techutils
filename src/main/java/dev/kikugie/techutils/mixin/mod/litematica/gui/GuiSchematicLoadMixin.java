package dev.kikugie.techutils.mixin.mod.litematica.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.kikugie.techutils.client.compat.axiom.BlueprintLoader;
import fi.dy.masa.litematica.gui.GuiSchematicLoad;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

/**
 * Adds an ability to load Axiom blueprints in Litematica
 */
@Condition("axiom")
@Mixin(targets = "fi/dy/masa/litematica/gui/GuiSchematicLoad$ButtonListener", remap = false)
public class GuiSchematicLoadMixin {
    @Shadow
    @Final
    private GuiSchematicLoad gui;

    // It workn't
    /*
    @WrapWithCondition(method = "actionPerformedWithButton", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/gui/GuiSchematicLoad;addMessage(Lfi/dy/masa/malilib/gui/Message$MessageType;Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 1))
    private boolean loadAxiomBlueprint(GuiSchematicLoad instance,
                                       Message.MessageType messageType,
                                       String s,
                                       Object[] objects,
                                       @Local WidgetFileBrowserBase.DirectoryEntry entry,
                                       @Local LocalRef<LitematicaSchematic> schematic,
                                       @Local(ordinal = 0) LocalBooleanRef warnType
    ) {
        if (!BlueprintLoader.isBlueprint(entry))
            return true;
        warnType.set(true);
        schematic.set(BlueprintLoader.toLitematic(entry.getFullPath(), this.gui));
        return false;
    }
    */

    @ModifyExpressionValue(method = "actionPerformedWithButton", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/FileType;fromFile(Ljava/io/File;)Lfi/dy/masa/litematica/util/FileType;"))
    private FileType checkForBlueprint(FileType original, @Local WidgetFileBrowserBase.DirectoryEntry entry, @Share("isAxiom") LocalBooleanRef isAxiom) {
        if (BlueprintLoader.isBlueprint(entry)) {
            isAxiom.set(true);
            return FileType.LITEMATICA_SCHEMATIC;
        }
        isAxiom.set(false);
        return original;
    }

    @WrapOperation(method = "actionPerformedWithButton", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/LitematicaSchematic;createFromFile(Ljava/io/File;Ljava/lang/String;)Lfi/dy/masa/litematica/schematic/LitematicaSchematic;"))
    private LitematicaSchematic loadBlueprint(File dir, String fileName, Operation<LitematicaSchematic> original, @Local(ordinal = 0) LocalBooleanRef warnText, @Share("isAxiom") LocalBooleanRef isAxiom) {
        if (!isAxiom.get())
            return original.call(dir, fileName);
        warnText.set(true);
        return BlueprintLoader.toLitematic(new File(dir, fileName), this.gui);
    }
}
