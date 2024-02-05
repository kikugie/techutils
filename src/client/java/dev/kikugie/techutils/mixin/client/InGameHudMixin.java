package dev.kikugie.techutils.mixin.client;

import com.google.common.math.IntMath;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.kikugie.techutils.TechUtilsClient;
import dev.kikugie.techutils.config.GeneralMisc;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final NumberFormat formatter = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
    @Unique
    private static boolean signUpForSpamProtectionProgram = false;

    static {
        formatter.setMaximumFractionDigits(1);
    }

    @ModifyArg(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toString(I)Ljava/lang/String;"))
    private int getCompactNum(int value) {
        if (!GeneralMisc.INSTANCE.getCompactScoreboard().getBooleanValue()) return value;
        return IntMath.pow(10, formatter.format(value).length() - 1);
    }

    @Inject(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0))
    private void shortenScore(CallbackInfo ci, @Local LocalRef<String> score) {
        if (!GeneralMisc.INSTANCE.getCompactScoreboard().getBooleanValue()) return;
        try {
            score.set(Formatting.RED + formatter.format(Integer.parseInt(Objects.requireNonNull(Formatting.strip(score.get())))));
        } catch (NumberFormatException e) {
            if (!signUpForSpamProtectionProgram) {
                TechUtilsClient.INSTANCE.getLOGGER().warn("Error in scoreboard value formatting!", e);
                signUpForSpamProtectionProgram = true;
            }
        }
    }
}