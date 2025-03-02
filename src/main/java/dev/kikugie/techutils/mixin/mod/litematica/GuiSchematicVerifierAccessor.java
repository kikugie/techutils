package dev.kikugie.techutils.mixin.mod.litematica;

import fi.dy.masa.litematica.gui.GuiSchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiSchematicVerifier.class, remap = false)
public interface GuiSchematicVerifierAccessor {
	@Accessor("verifier")
	SchematicVerifier getVerifier();
}
