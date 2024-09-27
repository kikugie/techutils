package dev.kikugie.techutils.feature.preview.gui;

import dev.kikugie.techutils.feature.preview.interaction.InteractionProfile;
import dev.kikugie.techutils.feature.preview.interaction.InteractionProfiles;
import dev.kikugie.techutils.feature.preview.model.PreviewRenderer;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Caches active preview renderers while GUI is open.
 */
public class PreviewRenderManager {
	@Nullable
	private static PreviewRenderManager instance;
	private final Map<DirectoryEntry, PreviewRenderer> cache = new HashMap<>();
	private final GuiSchematicBrowserBase gui;
	private final InteractionProfile profile;
	private PreviewRenderer current;

	private PreviewRenderManager(GuiSchematicBrowserBase gui) {
		this.gui = gui;
		this.profile = InteractionProfiles.get(InteractionProfiles.DRAG, this);
	}

	public static PreviewRenderManager init(GuiSchematicBrowserBase gui) {
		instance = new PreviewRenderManager(gui);
		return instance;
	}

	public static void close() {
		instance = null;
	}

	public static Optional<PreviewRenderManager> getInstance() {
		return Optional.ofNullable(instance);
	}

	private static LitematicaSchematic getSchematic(DirectoryEntry entry) {
		assert FileType.fromFile(entry.getFullPath()) == FileType.LITEMATICA_SCHEMATIC;
		return LitematicaSchematic.createFromFile(entry.getDirectory(), entry.getName(), FileType.LITEMATICA_SCHEMATIC);
	}

	public PreviewRenderer getOrCreateRenderer(DirectoryEntry entry) {
		PreviewRenderer renderer = this.cache.get(entry);
		if (renderer != null) {
			this.current = renderer;
			return renderer;
		}

		LitematicaSchematic schematic = getSchematic(entry);
		this.current = new PreviewRenderer(schematic, this.profile);
		this.cache.put(entry, this.current);
		return this.current;
	}

	public InteractionProfile profile() {
		return this.profile;
	}
}
