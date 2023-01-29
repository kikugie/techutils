package me.kikugie.techutils.event

import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback
import fi.dy.masa.malilib.hotkeys.IKeybind
import fi.dy.masa.malilib.hotkeys.KeyAction
import me.kikugie.techutils.config.Configs
import me.kikugie.techutils.config.InGameNotifier
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation

object KeyCallbacks {
    fun init() {
        Configs.LitematicConfigs.ROTATE_PLACEMENT.keybind.setCallback(RotatePlacementCallback())
        Configs.LitematicConfigs.MIRROR_PLACEMENT.keybind.setCallback(MirrorPlacementCallback())
    }

    private class RotatePlacementCallback : IHotkeyCallback {
        override fun onKeyAction(action: KeyAction, key: IKeybind): Boolean {
            if (key === Configs.LitematicConfigs.ROTATE_PLACEMENT.keybind) {
                val placement: SchematicPlacement =
                    DataManager.getSchematicPlacementManager().selectedSchematicPlacement
                        ?: return false
                placement.setRotation(
                    placement.rotation.rotate(BlockRotation.CLOCKWISE_90),
                    InGameNotifier.INSTANCE
                )
                return true
            }
            return false
        }
    }

    private class MirrorPlacementCallback : IHotkeyCallback {
        override fun onKeyAction(action: KeyAction, key: IKeybind): Boolean {
            if (key === Configs.LitematicConfigs.MIRROR_PLACEMENT.keybind) {
                val placement: SchematicPlacement =
                    DataManager.getSchematicPlacementManager().selectedSchematicPlacement
                        ?: return false

                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                val mirror: BlockMirror = when (placement.mirror) {
                    BlockMirror.NONE -> BlockMirror.LEFT_RIGHT
                    BlockMirror.LEFT_RIGHT -> BlockMirror.FRONT_BACK
                    BlockMirror.FRONT_BACK -> BlockMirror.NONE
                }
                placement.setMirror(mirror, InGameNotifier.INSTANCE)
                return true
            }
            return false
        }
    }
}