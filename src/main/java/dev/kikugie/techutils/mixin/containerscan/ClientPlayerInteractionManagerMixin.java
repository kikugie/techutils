package dev.kikugie.techutils.mixin.containerscan;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Definition(id = "ActionResult", type = ActionResult.class)
	@Definition(id = "getValue", method = "Lorg/apache/commons/lang3/mutable/MutableObject;getValue()Ljava/lang/Object;")
	@Expression("return @((ActionResult) ?.getValue())")
	@ModifyExpressionValue(method = "interactBlock", at = @At("MIXINEXTRAS:EXPRESSION"))
	private ActionResult recordContainer(ActionResult original, @Local(argsOnly = true) BlockHitResult hitResult) {
		if (original.isAccepted()) {
			InventoryOverlay.onContainerClick(hitResult);
		}
		return original;
	}
}
