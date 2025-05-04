package dev.kikugie.techutils.mixin.compactscoreboard;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.techutils.config.MiscConfigs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import java.text.NumberFormat;
import java.util.Locale;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Unique
	private static final NumberFormat FORMATTER = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);

	static {
		FORMATTER.setMaximumFractionDigits(1);
	}

	@WrapOperation(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toString(I)Ljava/lang/String;"))
	private String replaceWithCompactFormat$1(int score, Operation<String> original) {
		return MiscConfigs.COMPACT_SCOREBOARD.getBooleanValue()
			? FORMATTER.format(score)
			: original.call(score);
	}

	@ModifyVariable(
		method = "renderScoreboardSidebar",
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/util/Formatting;RED:Lnet/minecraft/util/Formatting;")
		),
		at = @At(value = "STORE", ordinal = 0)
	)
	private String replaceWithCompactFormat$2(String original, @Local ScoreboardPlayerScore playerScore) {
		return MiscConfigs.COMPACT_SCOREBOARD.getBooleanValue()
			? Formatting.RED + FORMATTER.format(playerScore.getScore())
			: original;
	}
}
