package me.kikugie.techutils.mixin;

import com.google.common.math.IntMath;
import me.kikugie.techutils.TechUtilsMod;
import me.kikugie.techutils.config.Configs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.text.NumberFormat;
import java.util.Locale;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final NumberFormat formatter = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
    private static boolean signUpForSpamProtectionProgram = false;

    static {
        formatter.setMaximumFractionDigits(1);
    }

    @ModifyArg(
            method = "renderScoreboardSidebar",
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/Integer;toString(I)Ljava/lang/String;")
    )
    private int getCompactNum(int value) {
        if (!Configs.MiscConfigs.COMPACT_SCOREBOARD.getBooleanValue()) return value;
        return IntMath.pow(10, formatter.format(value).length() - 1);
    }

    @ModifyVariable(
            method = "renderScoreboardSidebar",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I",
                    ordinal = 0),
            index = 22
    )
    private String modifyScore(String score) {
        if (!Configs.MiscConfigs.COMPACT_SCOREBOARD.getBooleanValue()) return score;
        try {
            return Formatting.RED + formatter.format(Integer.parseInt(Formatting.strip(score)));
        } catch (NumberFormatException e) {
            if (!signUpForSpamProtectionProgram) {
                TechUtilsMod.LOGGER.error("Error in scoreboard value formatting!");
                e.printStackTrace();
                signUpForSpamProtectionProgram = true;
            }
            return score;
        }
    }
}
