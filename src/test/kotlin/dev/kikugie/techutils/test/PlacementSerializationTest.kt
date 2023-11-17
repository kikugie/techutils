package dev.kikugie.techutils.test

import dev.kikugie.techutils.client.feature.serializer.RegionData
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.Vec3i
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@Suppress("BooleanLiteralArgument")
class PlacementSerializationTest {
    val DATA_1 = RegionData(
        true,
            false,
        false,
        Vec3i(1, 10, -13),
        BlockRotation.NONE,
        BlockMirror.NONE
    )

    @Test
    fun `test DATA_1`() {
        DATA_1.write()
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            SharedConstants.createGameVersion()
            Bootstrap.initialize()
        }
    }
}