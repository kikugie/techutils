package dev.kikugie.techutils.mixin.compactscoreboard;

import dev.kikugie.techutils.config.MiscConfigs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.text.NumberFormat;
import java.util.Locale;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Unique
	private static final NumberFormat FORMATTER = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);

	static {
		FORMATTER.setMaximumFractionDigits(1);
	}

	@SuppressWarnings("unchecked")
	@ModifyArg(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardObjective;getNumberFormatOr(Lnet/minecraft/scoreboard/number/NumberFormat;)Lnet/minecraft/scoreboard/number/NumberFormat;"))
	private <T extends net.minecraft.scoreboard.number.NumberFormat> T replaceWithCompactFormat(T format) {
		if (!MiscConfigs.COMPACT_SCOREBOARD.getBooleanValue())
			return format;

		return (T) new net.minecraft.scoreboard.number.NumberFormat() {
			@Override
			public MutableText format(int number) {
				return Text.literal(FORMATTER.format(number)).formatted(Formatting.RED);
			}

			@Override
			public NumberFormatType<T> getType() {
				return null;
			}
		};
	}
}
