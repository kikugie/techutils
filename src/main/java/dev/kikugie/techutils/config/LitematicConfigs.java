package dev.kikugie.techutils.config;

import com.google.common.collect.ImmutableList;
import dev.kikugie.techutils.feature.preview.interaction.InteractionProfiles;
import fi.dy.masa.malilib.config.options.*;

public class LitematicConfigs extends Configs.BaseConfigs {
    public static final ConfigBoolean RENDER_PREVIEW = new ConfigBoolean("renderPreview", true,
            "Show 3D render of selected litematic in Load Schematics menu\n(Works only for .litematic files)");
    public static final ConfigBoolean OVERRIDE_PREVIEW = new ConfigBoolean("overridePreview", false,
            "Show 3D render even if litematic has its own preview");
    public static final ConfigOptionList RENDER_ROTATION_MODE = new ConfigOptionList("rotationMode", InteractionProfiles.DRAG,
            """
                    Configure model rotation mode:
                    - DRAG: drag left mouse button in the viewport to rotate the model, drag right mouse button to pan the camera and scroll to zoom;
                    - POSITION: rotation follows horizontal mouse position on the screen;
                    - SPIN: rotate model at constant speed.""");
    public static final ConfigDouble ROTATION_FACTOR = new ConfigDouble("rotationFactor", 1, 0.1, 10,
            "Set model rotation sensitivity");
    public static final ConfigInteger RENDER_SLANT = new ConfigInteger("renderSlant", 30, 0, 60,
            "Set model vertical slant");
    public static final ConfigHotkey ROTATE_PLACEMENT = new ConfigHotkey("rotatePlacement", "R",
            "Rotate selected placement clockwise");
    public static final ConfigHotkey MIRROR_PLACEMENT = new ConfigHotkey("mirrorPlacement", "Y",
            "Cycle through selected placement's mirroring options");
    public static final ConfigBooleanHotkeyed INVENTORY_SCREEN_OVERLAY = new ConfigBooleanHotkeyed("inventoryScreenOverlay", true, "I, O", """
            Show layout of the container according to the litematic placement.
            Item colors match your placement block colors. By default its:
            - Light blue: missing item;
            - Orange: mismatched amount or nbt data;
            - Magenta: extra item that shouldn't be present;
            - Red: wrong item type.""");

    public LitematicConfigs() {
        super(ImmutableList.of(
                RENDER_PREVIEW,
                OVERRIDE_PREVIEW,
                RENDER_ROTATION_MODE,
                ROTATION_FACTOR,
                RENDER_SLANT,
                ROTATE_PLACEMENT,
                MIRROR_PLACEMENT,
                INVENTORY_SCREEN_OVERLAY
        ));
    }
}
