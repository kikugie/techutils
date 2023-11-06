package dev.kikugie.techutils.client.util

import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import dev.kikugie.techutils.client.TechUtilsClient
import net.minecraft.SharedConstants
import net.minecraft.block.BlockState
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps

val dataVersion
    get() = SharedConstants.getGameVersion().saveVersion.id

fun updatePalettedContainer(tag: NbtCompound, fromVersion: Int): NbtCompound {
    return if (!hasExpectedPaletteTag(tag)) {
        TechUtilsClient.LOGGER.warn("'palette' tag missing from PalettedContainer NBT, unable to upgrade...")
        tag
    } else if (fromVersion == dataVersion) {
        tag
    } else {
        val copy = tag.copy()
        val newPalette = NbtList()
        for (entry in copy.getList("palette", 10)) {
            val dynamic = Dynamic(NbtOps.INSTANCE, entry)
            val output =
                Schemas.getFixer().update(TypeReferences.BLOCK_STATE, dynamic, fromVersion, dataVersion)
            newPalette.add(output.value as NbtElement)
        }
        copy.put("palette", newPalette)
        copy
    }
}

fun updateBlockState(tag: NbtCompound?, fromVersion: Int): DataResult<BlockState> {
    val dynamic = Dynamic(NbtOps.INSTANCE, tag)
    val output = Schemas.getFixer().update(TypeReferences.BLOCK_STATE, dynamic, fromVersion, dataVersion)
    return BlockState.CODEC.parse(output)
}

private fun hasExpectedPaletteTag(tag: NbtCompound): Boolean {
    return if (!tag.contains("palette", 9)) {
        false
    } else {
        val listTag = tag["palette"] as NbtList?
        if (listTag == null) {
            false
        } else {
            listTag.isEmpty() || listTag.heldType.toInt() == 10
        }
    }
}