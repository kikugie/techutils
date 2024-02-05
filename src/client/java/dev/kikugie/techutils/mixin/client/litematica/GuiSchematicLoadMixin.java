package dev.kikugie.techutils.mixin.client.litematica;

import fi.dy.masa.litematica.gui.GuiSchematicLoad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds an ability to load Axiom blueprints in Litematica
 */

@Mixin(targets = "fi/dy/masa/litematica/gui/GuiSchematicLoad$ButtonListener", remap = false)
public class GuiSchematicLoadMixin {
    @Shadow
    @Final
    private GuiSchematicLoad gui;

    // TODO

//    @ModifyExpressionValue(method = "actionPerformedWithButton", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/FileType;fromFile(Ljava/io/File;)Lfi/dy/masa/litematica/util/FileType;"))
//    private FileType checkForBlueprint(FileType original, @Local WidgetFileBrowserBase.DirectoryEntry entry, @Share("isAxiom") LocalBooleanRef isAxiom) {
//        if (Files.getFileExtension(entry.getName()).equals("bp")) {
//            isAxiom.set(true);
//            return FileType.LITEMATICA_SCHEMATIC;
//        }
//        isAxiom.set(false);
//        return original;
//    }
//
//    @WrapOperation(method = "actionPerformedWithButton", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/schematic/LitematicaSchematic;createFromFile(Ljava/io/File;Ljava/lang/String;)Lfi/dy/masa/litematica/schematic/LitematicaSchematic;"))
//    private LitematicaSchematic loadBlueprint(File dir, String fileName, Operation<LitematicaSchematic> original, @Local(ordinal = 0) LocalBooleanRef warnText, @Share("isAxiom") LocalBooleanRef isAxiom) {
//        if (!isAxiom.get())
//            return original.call(dir, fileName);
//        warnText.set(true);
//        try {
//            return BlueprintLoader.INSTANCE.toLitematic(new File(dir, fileName).toPath(), this.gui);
//        } catch (Exception e) {
//            TechUtilsClient.INSTANCE.getLOGGER().error("Failed to load blueprint", e);
//            return null;
//        }
//    }
}
