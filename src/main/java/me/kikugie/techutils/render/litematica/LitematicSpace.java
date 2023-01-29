package me.kikugie.techutils.render.litematica;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import net.minecraft.util.math.Box;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LitematicSpace {
    public final Map<String, LitematicRegionBlockView> regionBlockViewMap;
    public final Map<String, Box> boxes;
    public final Box fullBox;

    public LitematicSpace(LitematicaSchematic schematic) {
        this.boxes = getConvertedBoxes(schematic.getAreas());
        this.fullBox = getFullBox(boxes);
        this.regionBlockViewMap = getRegionBlockViewMap(schematic);
    }

    private Map<String, LitematicRegionBlockView> getRegionBlockViewMap(LitematicaSchematic schematic) {
        Collection<String> areas = schematic.getAreas().keySet();
        HashMap<String, LitematicRegionBlockView> containers = new HashMap<>(areas.size());
        areas.forEach(name -> containers.put(name, new LitematicRegionBlockView(schematic.getSubRegionContainer(name), boxes.get(name))));
        return containers;
    }

    private Map<String, Box> getConvertedBoxes(Map<String, fi.dy.masa.litematica.selection.Box> areas) {
        HashMap<String, Box> boxes = new HashMap<>(areas.size());
        areas.forEach((name, region) -> boxes.put(name, new Box(region.getPos1(), region.getPos2())));
        return boxes;
    }

    private Box getFullBox(Map<String, Box> boxes) {
        int[] dims = {0, 0, 0, 0, 0, 0};
        boxes.forEach((name, box) -> {
            dims[0] = Math.min(dims[0], (int) box.minX);
            dims[1] = Math.min(dims[1], (int) box.minY);
            dims[2] = Math.min(dims[2], (int) box.minZ);
            dims[3] = Math.max(dims[3], (int) box.maxX);
            dims[4] = Math.max(dims[4], (int) box.maxY);
            dims[5] = Math.max(dims[5], (int) box.maxZ);
        });
        return new Box(dims[0], dims[1], dims[2], dims[3], dims[4], dims[5]);
    }
}
