package io.github.antiqueatlasmultiplayer;

import io.github.antiqueatlasmultiplayer.network.PlayerPositionsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Common/server side: registers the payload and periodically broadcasts every
 * player's position to the clients that have the mod installed.
 */
public class AntiqueAtlasMultiplayer implements ModInitializer {
	public static final String MOD_ID = "antique_atlas_multiplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static AntiqueAtlasMultiplayerConfig CONFIG;

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		CONFIG = AntiqueAtlasMultiplayerConfig.load();
		PayloadTypeRegistry.playS2C().register(PlayerPositionsPayload.ID, PlayerPositionsPayload.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(AntiqueAtlasMultiplayer::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		if (server.getTicks() % CONFIG.updateIntervalTicks != 0) return;
		List<ServerPlayerEntity> online = server.getPlayerManager().getPlayerList();
		if (online.size() < 2) return;

		List<PlayerPositionsPayload.Entry> visible = new ArrayList<>(online.size());
		for (ServerPlayerEntity player : online) {
			if (!isVisibleOnAtlas(player)) continue;
			visible.add(new PlayerPositionsPayload.Entry(
				player.getUuid(),
				player.getGameProfile().getName(),
				player.getWorld().getRegistryKey(),
				player.getX(),
				player.getY(),
				player.getZ(),
				player.getYaw()
			));
		}

		for (ServerPlayerEntity receiver : online) {
			if (!ServerPlayNetworking.canSend(receiver, PlayerPositionsPayload.ID)) continue;
			List<PlayerPositionsPayload.Entry> others = new ArrayList<>(visible.size());
			for (PlayerPositionsPayload.Entry entry : visible) {
				if (!entry.uuid().equals(receiver.getUuid())) others.add(entry);
			}
			ServerPlayNetworking.send(receiver, new PlayerPositionsPayload(others));
		}
	}

	private static boolean isVisibleOnAtlas(ServerPlayerEntity player) {
		if (!CONFIG.showSpectators && player.isSpectator()) return false;
		if (!CONFIG.showInvisible && player.hasStatusEffect(StatusEffects.INVISIBILITY)) return false;
		if (!CONFIG.showSneaking && player.isSneaking()) return false;
		return true;
	}
}
