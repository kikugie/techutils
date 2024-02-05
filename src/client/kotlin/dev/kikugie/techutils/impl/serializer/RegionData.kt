package dev.kikugie.techutils.impl.serializer

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.kikugie.techutils.TechUtilsClient
import dev.kikugie.techutils.util.InGameNotifier
import dev.kikugie.techutils.mixin.client.litematica.SubRegionPlacementAccessor
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

data class RegionData(
    val enabled: Boolean,
    val ignoreEntities: Boolean,
    val position: BlockPos,
    val rotation: BlockRotation,
    val mirror: BlockMirror
) {
    constructor(placement: SchematicPlacement) : this(
        placement.isEnabled,
        placement.ignoreEntities(),
        placement.origin,
        placement.rotation,
        placement.mirror,
    )

    constructor(subregion: SubRegionPlacement) : this(
        subregion.isEnabled,
        subregion.ignoreEntities(),
        subregion.pos,
        subregion.rotation,
        subregion.mirror
    )

    fun apply(placement: SchematicPlacement) {
        if (placement.isLocked) placement.toggleLocked()

        // None of these should send a message
        if (placement.isEnabled != enabled) placement.toggleEnabled()
        if (placement.ignoreEntities() != ignoreEntities) placement.toggleIgnoreEntities(InGameNotifier)
        placement.setOrigin(position, InGameNotifier)
        placement.setRotation(rotation, InGameNotifier)
        placement.setMirror(mirror, InGameNotifier)
    }

    fun apply(subregion: SubRegionPlacement) {
        val accessor = subregion as SubRegionPlacementAccessor
        accessor.forceSetEnabled(enabled)
        accessor.forceSetIgnoreEntities(ignoreEntities)
        accessor.forceSetPos(position)
        accessor.forceSetRotation(rotation)
        accessor.forceSetMirror(mirror)
    }

    @Throws(RuntimeException::class)
    fun encode(): JsonElement = CODEC.encodeStart(JsonOps.INSTANCE, this)
        .getOrThrow(false) { TechUtilsClient.LOGGER.warn("Failed to save placement data: $it") }

    companion object {
        val CODEC: Codec<RegionData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter { it.enabled },
                Codec.BOOL.optionalFieldOf("ignoreEntities", false).forGetter { it.ignoreEntities },
                BlockPos.CODEC.optionalFieldOf("position", BlockPos.ORIGIN).forGetter { it.position },
                BlockRotation.CODEC.optionalFieldOf("rotation", BlockRotation.NONE).forGetter { it.rotation },
                BlockMirror.CODEC.optionalFieldOf("mirror", BlockMirror.NONE).forGetter { it.mirror }
            ).apply(instance, ::RegionData)
        }

        @Throws(RuntimeException::class)
        fun decode(json: JsonElement): RegionData = CODEC.decode(JsonOps.INSTANCE, json)
                .getOrThrow(false) { TechUtilsClient.LOGGER.warn("Failed to read placement data: $it") }.first
    }
}
