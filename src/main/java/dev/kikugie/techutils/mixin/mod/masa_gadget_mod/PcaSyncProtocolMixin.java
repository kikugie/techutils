package dev.kikugie.techutils.mixin.mod.masa_gadget_mod;

import com.llamalad7.mixinextras.sugar.Local;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import dev.kikugie.techutils.util.EntitiesDataStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PcaSyncProtocol.class, remap = false)
public class PcaSyncProtocolMixin {
	@Inject(method = "updateBlockEntityHandler", at = @At("TAIL"))
	private static void updateDataStorage(CallbackInfo ci, @Local BlockPos pos, @Local NbtCompound tag) {
		EntitiesDataStorage.getInstance().handleBlockEntityData(pos, tag, false);
	}
}
