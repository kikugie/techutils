package me.kikugie.techutils.render.litematica

import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.Box
import net.minecraft.util.math.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class LitematicSpace(schematic: LitematicaSchematic?) {
    val regionBlockViewMap: Map<String?, LitematicRegionBlockView?>
    val boxes: Map<String?, net.minecraft.util.math.Box?>
    val fullBox: net.minecraft.util.math.Box

    init {
        boxes = getConvertedBoxes(schematic!!.areas)
        fullBox = getFullBox(boxes)
        regionBlockViewMap = getRegionBlockViewMap(schematic)
    }

    private fun getRegionBlockViewMap(schematic: LitematicaSchematic?): Map<String?, LitematicRegionBlockView?> {
        val areas: Collection<String> = schematic!!.areas.keys
        val containers = HashMap<String?, LitematicRegionBlockView?>(areas.size)
        areas.forEach(Consumer { name: String? ->
            containers[name] = LitematicRegionBlockView(
                schematic.getSubRegionContainer(name), boxes[name]
            )
        })
        return containers
    }

    private fun getConvertedBoxes(areas: Map<String, Box>): Map<String?, net.minecraft.util.math.Box?> {
        val boxes = HashMap<String?, net.minecraft.util.math.Box?>(areas.size)
        areas.forEach { (name: String?, region: Box) -> boxes[name] = Box(region.pos1, region.pos2) }
        return boxes
    }

    private fun getFullBox(boxes: Map<String?, net.minecraft.util.math.Box?>): net.minecraft.util.math.Box {
        val dims = intArrayOf(0, 0, 0, 0, 0, 0)
        boxes.forEach { (_: String?, box: net.minecraft.util.math.Box?) ->
            dims[0] = min(dims[0], box!!.minX.toInt())
            dims[1] = min(dims[1], box.minY.toInt())
            dims[2] = min(dims[2], box.minZ.toInt())
            dims[3] = max(dims[3], box.maxX.toInt())
            dims[4] = max(dims[4], box.maxY.toInt())
            dims[5] = max(dims[5], box.maxZ.toInt())
        }
        return Box(
            dims[0].toDouble(),
            dims[1].toDouble(),
            dims[2].toDouble(),
            dims[3].toDouble(),
            dims[4].toDouble(),
            dims[5].toDouble()
        )
    }
}