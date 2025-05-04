package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.client.network.DataQueryHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataQueryHandler.class)
public interface DataQueryHandlerAccessor {
	@Accessor("expectedTransactionId")
	int expectedTransactionId();
}
