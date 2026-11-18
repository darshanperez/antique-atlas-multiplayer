package io.github.antiqueatlasmultiplayer.client;

import io.github.antiqueatlasmultiplayer.network.PlayerPositionsPayload;
import net.minecraft.util.Util;

import java.util.List;

/**
 * Latest player list received from the server. Doesn't reference any Antique
 * Atlas/Surveyor classes, so the mod loads even when they aren't installed.
 */
public final class RemotePlayers {
	/** If the server stops sending data for this long, the data is considered stale. */
	private static final long STALE_AFTER_MS = 5_000;

	private static volatile List<PlayerPositionsPayload.Entry> players = List.of();
	private static volatile long lastUpdate = 0;

	private RemotePlayers() {
	}

	static void update(List<PlayerPositionsPayload.Entry> newPlayers) {
		players = List.copyOf(newPlayers);
		lastUpdate = Util.getMeasuringTimeMs();
	}

	static void clear() {
		players = List.of();
		lastUpdate = 0;
	}

	/** {@code true} if the server has the mod and is sending us positions. */
	public static boolean hasServerData() {
		return lastUpdate != 0 && Util.getMeasuringTimeMs() - lastUpdate < STALE_AFTER_MS;
	}

	public static List<PlayerPositionsPayload.Entry> get() {
		return hasServerData() ? players : List.of();
	}
}
