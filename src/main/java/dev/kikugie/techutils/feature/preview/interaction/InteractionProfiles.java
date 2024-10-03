package dev.kikugie.techutils.feature.preview.interaction;

import dev.kikugie.techutils.feature.preview.gui.PreviewRenderManager;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.minecraft.text.Text;

import java.util.NoSuchElementException;
import java.util.function.Function;

public enum InteractionProfiles implements IConfigOptionListEntry {
	DRAG(DragProfile::new),
	SPIN(SpinProfile::new),
	POS(MousePosProfile::new);

	private static final String TRANSLATION_KEY_BASE = "techutils.config.preview.profile.";

	public final String value = this.name().toLowerCase();

	public final Text name = Text.translatable(TRANSLATION_KEY_BASE + this.value);
	public final Text description = Text.translatable(TRANSLATION_KEY_BASE + this.value + ".description");
	private final Function<PreviewRenderManager, ? extends InteractionProfile> supplier;

	InteractionProfiles(Function<PreviewRenderManager, ? extends InteractionProfile> supplier) {
		this.supplier = supplier;
	}

	public static InteractionProfile get(InteractionProfiles profile, PreviewRenderManager manager) {
		return profile.supplier.apply(manager);
	}

	@Override
	public String getStringValue() {
		return this.value;
	}

	@Override
	public String getDisplayName() {
		return this.name.getString();
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward) {
		int mod = forward ? 1 : -1;
		return values()[(this.ordinal() + mod) % values().length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value) {
		return fromStringStatic(value);
	}

	public static InteractionProfiles fromStringStatic(String value) {
		try {
			return InteractionProfiles.valueOf(value.toUpperCase());
		} catch (NoSuchElementException e) {
			return InteractionProfiles.DRAG;
		}
	}
}
