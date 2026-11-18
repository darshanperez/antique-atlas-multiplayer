package io.github.antiqueatlasmultiplayer;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Server-side config: {@code config/antique_atlas_multiplayer.properties}.
 */
public final class AntiqueAtlasMultiplayerConfig {
	/** How often positions are sent, in ticks (20 ticks = 1 second). */
	public int updateIntervalTicks = 10;
	/** Show players in spectator mode. */
	public boolean showSpectators = false;
	/** Show invisible players (invisibility effect). */
	public boolean showInvisible = false;
	/** Show players who are sneaking. */
	public boolean showSneaking = true;

	public static AntiqueAtlasMultiplayerConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(AntiqueAtlasMultiplayer.MOD_ID + ".properties");
		AntiqueAtlasMultiplayerConfig config = new AntiqueAtlasMultiplayerConfig();
		Properties props = new Properties();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				props.load(reader);
			} catch (IOException e) {
				AntiqueAtlasMultiplayer.LOGGER.warn("[Antique Atlas Multiplayer] Could not read {}, using defaults", path, e);
			}
		}
		config.updateIntervalTicks = Math.max(1, parseInt(props.getProperty("updateIntervalTicks"), config.updateIntervalTicks));
		config.showSpectators = parseBool(props.getProperty("showSpectators"), config.showSpectators);
		config.showInvisible = parseBool(props.getProperty("showInvisible"), config.showInvisible);
		config.showSneaking = parseBool(props.getProperty("showSneaking"), config.showSneaking);
		config.save(path);
		return config;
	}

	private void save(Path path) {
		Properties props = new Properties();
		props.setProperty("updateIntervalTicks", Integer.toString(updateIntervalTicks));
		props.setProperty("showSpectators", Boolean.toString(showSpectators));
		props.setProperty("showInvisible", Boolean.toString(showInvisible));
		props.setProperty("showSneaking", Boolean.toString(showSneaking));
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				props.store(writer, "Antique Atlas Multiplayer (server side). updateIntervalTicks: 20 = 1 second.");
			}
		} catch (IOException e) {
			AntiqueAtlasMultiplayer.LOGGER.warn("[Antique Atlas Multiplayer] Could not save {}", path, e);
		}
	}

	private static int parseInt(String value, int fallback) {
		if (value == null) return fallback;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static boolean parseBool(String value, boolean fallback) {
		return value == null ? fallback : Boolean.parseBoolean(value.trim());
	}
}
