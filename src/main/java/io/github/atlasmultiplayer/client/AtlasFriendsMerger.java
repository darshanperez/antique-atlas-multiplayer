package io.github.atlasmultiplayer.client;

import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import io.github.atlasmultiplayer.network.PlayerPositionsPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merges Surveyor's "friends" (players sharing their map) with every player on
 * the server, so Antique Atlas draws all of them.
 *
 * <p>Only loaded from the Antique Atlas mixin, so referencing Surveyor classes
 * here is safe.
 */
public final class AtlasFriendsMerger {
	private AtlasFriendsMerger() {
	}

	public static Map<UUID, PlayerSummary> merge(Map<UUID, PlayerSummary> original) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return original;

		UUID selfUuid = client.player.getUuid();
		String selfName = client.player.getGameProfile().getName();
		Map<String, Map.Entry<UUID, PlayerSummary>> live = collectLivePlayers(client, selfUuid, selfName);
		if (live.isEmpty()) return original;

		// Antique Atlas expects the local player last (drawn on top of everyone else).
		UUID surveyorSelf = SurveyorClient.getClientUuid();
		Map<UUID, PlayerSummary> result = new LinkedHashMap<>();
		Map.Entry<UUID, PlayerSummary> self = null;
		for (Map.Entry<UUID, PlayerSummary> entry : original.entrySet()) {
			if (entry.getKey().equals(surveyorSelf)) {
				self = entry;
				continue;
			}
			// If we have a live position for the same player, prefer it over Surveyor's
			// (which may be an old position of an offline player).
			Map.Entry<UUID, PlayerSummary> livePlayer = live.remove(entry.getValue().username());
			if (livePlayer != null) {
				result.put(entry.getKey(), livePlayer.getValue());
			} else {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		for (Map.Entry<UUID, PlayerSummary> entry : live.values()) {
			result.putIfAbsent(entry.getKey(), entry.getValue());
		}
		if (self != null) result.put(self.getKey(), self.getValue());
		return result;
	}

	/** Visible players keyed by name (names are unique on a server). */
	private static Map<String, Map.Entry<UUID, PlayerSummary>> collectLivePlayers(MinecraftClient client, UUID selfUuid, String selfName) {
		Map<String, Map.Entry<UUID, PlayerSummary>> live = new HashMap<>();
		if (RemotePlayers.hasServerData()) {
			// The server has the mod: we know every player, at any distance.
			for (PlayerPositionsPayload.Entry entry : RemotePlayers.get()) {
				if (entry.uuid().equals(selfUuid) || entry.name().equals(selfName)) continue;
				PlayerEntity entity = client.world.getPlayerByUuid(entry.uuid());
				PlayerSummary summary = entity != null
					// If the player is loaded nearby, use the entity for smooth movement.
					? new PlayerSummary.PlayerEntitySummary(entity)
					: new PlayerSummary.OfflinePlayerSummary(null, entry.name(), entry.dimension(), new Vec3d(entry.x(), entry.y(), entry.z()), entry.yaw(), true);
				live.put(entry.name(), Map.entry(entry.uuid(), summary));
			}
		} else {
			// Server without the mod: we can only show players loaded within our render distance.
			for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
				if (player == client.player || player.getUuid().equals(selfUuid)) continue;
				if (player.isSpectator() || player.isInvisibleTo(client.player)) continue;
				String name = player.getGameProfile().getName();
				live.put(name, Map.entry(player.getUuid(), new PlayerSummary.PlayerEntitySummary(player)));
			}
		}
		return live;
	}
}
