package io.github.antiqueatlasmultiplayer.mixin;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.surveyor.PlayerSummary;
import io.github.antiqueatlasmultiplayer.client.AtlasFriendsMerger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

/**
 * Antique Atlas gets the list of players to draw (on the atlas screen and on the
 * handheld atlas) from {@code AntiqueAtlas.getOrderedFriends()}.
 * We add every other player on the server to it.
 */
@Mixin(value = AntiqueAtlas.class, remap = false)
public abstract class AntiqueAtlasMixin {
	@Inject(method = "getOrderedFriends", at = @At("RETURN"), cancellable = true)
	private static void antiqueAtlasMultiplayer$addAllPlayers(CallbackInfoReturnable<Map<UUID, PlayerSummary>> cir) {
		cir.setReturnValue(AtlasFriendsMerger.merge(cir.getReturnValue()));
	}
}
