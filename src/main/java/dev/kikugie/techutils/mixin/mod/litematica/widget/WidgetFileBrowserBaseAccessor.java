package dev.kikugie.techutils.mixin.mod.litematica.widget;

import fi.dy.masa.malilib.gui.interfaces.IFileBrowserIconProvider;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WidgetFileBrowserBase.class, remap = false)
public interface WidgetFileBrowserBaseAccessor {

    @Accessor
    IFileBrowserIconProvider getIconProvider();

    @Mutable
    @Accessor
    void setIconProvider(@NotNull IFileBrowserIconProvider iconProvider);
}
