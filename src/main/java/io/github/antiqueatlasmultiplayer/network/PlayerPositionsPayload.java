package io.github.antiqueatlasmultiplayer.network;

import io.github.antiqueatlasmultiplayer.AntiqueAtlasMultiplayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server -> client: positions of every online player (except the receiver).
 */
public record PlayerPositionsPayload(List<Entry> players) implements CustomPayload {
	public static final Id<PlayerPositionsPayload> ID = new Id<>(AntiqueAtlasMultiplayer.id("player_positions"));
	public static final PacketCodec<PacketByteBuf, PlayerPositionsPayload> CODEC = PacketCodec.of(PlayerPositionsPayload::write, PlayerPositionsPayload::read);

	/** Defensive limit so we never accept absurdly large packets. */
	private static final int MAX_PLAYERS = 4096;

	public record Entry(UUID uuid, String name, RegistryKey<World> dimension, double x, double y, double z, float yaw) {
		void write(PacketByteBuf buf) {
			buf.writeUuid(uuid);
			buf.writeString(name, 16);
			buf.writeRegistryKey(dimension);
			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeFloat(yaw);
		}

		static Entry read(PacketByteBuf buf) {
			return new Entry(
				buf.readUuid(),
				buf.readString(16),
				buf.readRegistryKey(RegistryKeys.WORLD),
				buf.readDouble(),
				buf.readDouble(),
				buf.readDouble(),
				buf.readFloat()
			);
		}
	}

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(players.size());
		for (Entry entry : players) entry.write(buf);
	}

	private static PlayerPositionsPayload read(PacketByteBuf buf) {
		int size = buf.readVarInt();
		if (size < 0 || size > MAX_PLAYERS) throw new IllegalArgumentException("Too many players in payload: " + size);
		List<Entry> players = new ArrayList<>(size);
		for (int i = 0; i < size; i++) players.add(Entry.read(buf));
		return new PlayerPositionsPayload(players);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
