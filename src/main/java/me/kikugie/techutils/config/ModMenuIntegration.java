package me.kikugie.techutils.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.kikugie.techutils.render.gui.ConfigGui;

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
