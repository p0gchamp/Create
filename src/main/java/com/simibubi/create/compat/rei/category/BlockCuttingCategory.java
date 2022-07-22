package com.simibubi.create.compat.rei.category;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.rei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class BlockCuttingCategory extends CreateRecipeCategory<CondensedBlockCuttingRecipe> {

	private AnimatedSaw saw = new AnimatedSaw();

	public BlockCuttingCategory(Info<CondensedBlockCuttingRecipe> info) {
		super(info);
	}

	@Override
	public void addWidgets(CreateDisplay<CondensedBlockCuttingRecipe> display, List<Widget> ingredients, Point origin) {
		ingredients.add(basicSlot(point(origin.x + 5, origin.y + 5))
				.markInput()
				.entries(display.getInputEntries().get(0)));

		List<List<ItemStack>> results = display.getRecipe().getCondensedOutputs();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 5) * 19;
			int yOffset = (outputIndex / 5) * -19;

			ingredients.add(basicSlot(point(origin.x + 78 + xOffset, origin.y + 48 + yOffset))
					.markOutput()
					.entries(EntryIngredients.ofItemStacks(results.get(outputIndex))));
		}
	}

	@Override
	public void draw(CondensedBlockCuttingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(matrixStack, 4, 4);
		int size = Math.min(recipe.getOutputs().size(), 15);
		for (int i = 0; i < size; i++) {
			int xOffset = (i % 5) * 19;
			int yOffset = (i / 5) * -19;
			AllGuiTextures.JEI_SLOT.render(matrixStack, 77 + xOffset, 47 + yOffset);
		}
		AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 31, 6);
		AllGuiTextures.JEI_SHADOW.render(matrixStack, 33 - 17, 37 + 13);
		saw.draw(matrixStack, 33, 37);
	}

	public static class CondensedBlockCuttingRecipe extends StonecutterRecipe {

		List<ItemStack> outputs = new ArrayList<>();

		public CondensedBlockCuttingRecipe(Ingredient ingredient) {
			super(new ResourceLocation(""), "", ingredient, ItemStack.EMPTY);
		}

		public void addOutput(ItemStack stack) {
			outputs.add(stack);
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		public List<List<ItemStack>> getCondensedOutputs() {
			List<List<ItemStack>> result = new ArrayList<>();
			int index = 0;
			boolean firstPass = true;
			for (ItemStack itemStack : outputs) {
				if (firstPass)
					result.add(new ArrayList<>());
				result.get(index).add(itemStack);
				index++;
				if (index >= 15) {
					index = 0;
					firstPass = false;
				}
			}
			return result;
		}

		@Override
		public boolean isSpecial() {
			return true;
		}

		public static List<CondensedBlockCuttingRecipe> condenseRecipes(List<Recipe<?>> stoneCuttingRecipes) {
			List<CondensedBlockCuttingRecipe> condensed = new ArrayList<>();
			Recipes: for (Recipe<?> recipe : stoneCuttingRecipes) {
				Ingredient i1 = recipe.getIngredients().get(0);
				for (CondensedBlockCuttingRecipe condensedRecipe : condensed) {
					if (ItemHelper.matchIngredients(i1, condensedRecipe.getIngredients().get(0))) {
						condensedRecipe.addOutput(recipe.getResultItem());
						continue Recipes;
					}
				}
				CondensedBlockCuttingRecipe cr = new CondensedBlockCuttingRecipe(i1);
				cr.addOutput(recipe.getResultItem());
				condensed.add(cr);
			}
			return condensed;
		}

	}

}
