package io.github.antiqueatlasmultiplayer.client;

import io.github.antiqueatlasmultiplayer.network.PlayerPositionsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AntiqueAtlasMultiplayerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(PlayerPositionsPayload.ID, (payload, context) -> RemotePlayers.update(payload.players()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RemotePlayers.clear());
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> RemotePlayers.clear());
	}
}
