package dev.kikugie.techutils.client.feature.serializer

import com.google.gson.*
import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.mixin.mod.litematica.placement.SchematicPlacementAccessor
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement

object PlacementSerializer {
    // Map key for the placement root. Let's hope no one names a subregion like this
    private const val KEY = ""
    private val gson: Gson = GsonBuilder().serializeNulls().create()

    fun serialize(placement: SchematicPlacement): String? {
        return try {
            val json = gson.toJson(read(placement))
            json.toString()
        } catch (e: RuntimeException) {
            TechUtilsClient.LOGGER.info("Failed to serialize placement $placement", e)
            null
        }
    }

    fun deserialize(data: String, placement: SchematicPlacement): Boolean {
        return try {
            val json = JsonParser.parseString(data).asJsonObject
            write(placement, json)
            true
        } catch (e: RuntimeException) {
            TechUtilsClient.LOGGER.info("Failed to deserialize placement $placement", e)
            false
        }
    }

    @Throws(RuntimeException::class)
    fun read(placement: SchematicPlacement): JsonObject {
        val json = JsonObject()
        val placementData = RegionData(placement)
        json.add(KEY, placementData.encode())

        placement.allSubRegionsPlacements.forEach {
            if (!it.isRegionPlacementModifiedFromDefault) return@forEach
            val regionData = RegionData(it)
            json.add(it.name, if (regionData != placementData) regionData.encode() else JsonNull.INSTANCE)
        }
        return json
    }

    @Throws(RuntimeException::class)
    fun write(placement: SchematicPlacement, json: JsonObject) {
        val placementData = RegionData.decode(
            json.get(KEY) ?: throw IllegalStateException("Missing data for placement ${placement.name}")
        )
        placementData.apply(placement)
        val subregions = placement.allSubRegionsPlacements.associateBy { it.name }

        json.asMap().forEach { (key, it) ->
            if (key == KEY) return@forEach
            if (!subregions.contains(key)) throw IllegalStateException("Subregion $key doesn't exist in the placement")
            val region = subregions[key]
                ?: throw IllegalStateException("Subregion $key is null. How is this even possible?!")
            if (it == JsonNull.INSTANCE)
                placementData.apply(region)
            else
                RegionData.decode(it).apply(region)
        }
        (placement as SchematicPlacementAccessor).invokeOnModified(null, DataManager.getSchematicPlacementManager())
    }
}