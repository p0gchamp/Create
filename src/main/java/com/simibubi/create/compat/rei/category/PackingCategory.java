package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.rei.category.animations.AnimatedPress;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class PackingCategory extends BasinCategory {

	private AnimatedPress press = new AnimatedPress(true);
	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
	private PackingType type;

	enum PackingType {
		AUTO_SQUARE, COMPACTING;
	}

	public static PackingCategory standard(CreateRecipeCategory.Info<BasinRecipe> info) {
		return new PackingCategory(info, PackingType.COMPACTING);
	}

	public static PackingCategory autoSquare(CreateRecipeCategory.Info<BasinRecipe> info) {
		return new PackingCategory(info, PackingType.AUTO_SQUARE);
	}

	protected PackingCategory(CreateRecipeCategory.Info<BasinRecipe> info, PackingCategory.PackingType type) {
		super(info, type != PackingType.AUTO_SQUARE);
		this.type = type;
	}

	@Override
	public void addWidgets(CreateDisplay<BasinRecipe> display, List<Widget> ingredients, Point origin) {
		BasinRecipe recipe = display.getRecipe();
		if (type == PackingType.COMPACTING) {
			super.addWidgets(display, ingredients, origin);
			return;
		}

		int i = 0;

		NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
		int size = ingredients2.size();
		int rows = size == 4 ? 2 : 3;
		while (i < size) {
			Ingredient ingredient = ingredients2.get(i);
			ingredients.add(basicSlot(new Point(origin.x + (rows == 2 ? 26 : 17) + (i % rows) * 19 + 1, origin.y + 50 - (i / rows) * 19 + 1))
					.markInput()
					.entries(EntryIngredients.ofIngredient(ingredient)));
			i++;
		}

		ingredients.add(basicSlot(point(origin.x + 142, origin.y + 51))
				.markOutput()
				.entries(EntryIngredients.of(recipe.getResultItem())));
	}

	@Override
	public void draw(BasinRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		if (type == PackingType.COMPACTING) {
			super.draw(recipe, matrixStack, mouseX, mouseY);

		} else {
			NonNullList<Ingredient> ingredients2 = recipe.getIngredients();
			int size = ingredients2.size();
			int rows = size == 4 ? 2 : 3;
			for (int i = 0; i < size; i++)
				AllGuiTextures.JEI_SLOT.render(matrixStack, (rows == 2 ? 26 : 17) + (i % rows) * 19,
					50 - (i / rows) * 19);
			AllGuiTextures.JEI_SLOT.render(matrixStack, 141, 50);
			AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 136, 32);
			AllGuiTextures.JEI_SHADOW.render(matrixStack, 81, 68);
		}

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(matrixStack, getDisplayWidth(null) / 2 + 3, 55);
		press.draw(matrixStack, getDisplayWidth(null) / 2 + 3, 34);
	}

}
