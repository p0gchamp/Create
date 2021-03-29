package com.simibubi.create.lib.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.event.MouseScrolledCallback;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), method = "onMouseScroll(JDD)V")
	private void onMouseScroll(PlayerInventory inventory, double delta) {
		boolean cancelled = MouseScrolledCallback.EVENT.invoker().onMouseScrolled(delta);
		if (!cancelled) {
			inventory.scrollInHotbar(delta);
		}
	}
}
