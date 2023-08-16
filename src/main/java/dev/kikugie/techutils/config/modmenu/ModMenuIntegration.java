package dev.kikugie.techutils.config.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kikugie.techutils.config.ConfigGui;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigGui gui = new ConfigGui();
            gui.setParent(screen);
            return gui;
        };
    }
}
