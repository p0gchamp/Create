package com.simibubi.create.compat.rei.category;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.rei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.rei.category.animations.AnimatedMixer;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

@ParametersAreNonnullByDefault
public class MixingCategory extends BasinCategory {

	private final AnimatedMixer mixer = new AnimatedMixer();
	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
	MixingType type;

	enum MixingType {
		MIXING, AUTO_SHAPELESS, AUTO_BREWING
	}

	public static MixingCategory standard(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.MIXING);
	}

	public static MixingCategory autoShapeless(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.AUTO_SHAPELESS);
	}

	public static MixingCategory autoBrewing(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.AUTO_BREWING);
	}

	protected MixingCategory(Info<BasinRecipe> info, MixingType type) {
		super(info, type != MixingType.AUTO_SHAPELESS);
		this.type = type;
	}

	@Override
	public void draw(BasinRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		super.draw(recipe, matrixStack, mouseX, mouseY);
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(matrixStack, getDisplayWidth(null) / 2 + 3, 55);
		mixer.draw(matrixStack, getDisplayWidth(null) / 2 + 3, 34);
	}

}
