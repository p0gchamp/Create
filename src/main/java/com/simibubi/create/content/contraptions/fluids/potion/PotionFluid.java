package com.simibubi.create.content.contraptions.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.foundation.utility.NBTHelper;

import io.github.fabricators_of_create.porting_lib.util.FluidAttributes;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public class PotionFluid extends VirtualFluid {

	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack of(long amount, Potion potion) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
				.getSource(), amount);
		return addPotionToFluidStack(fluidStack, potion);
	}

	public static FluidStack withEffects(long amount, Potion potion, List<MobEffectInstance> customEffects) {
		FluidStack fluidStack = of(amount, potion);
		return appendEffects(fluidStack, customEffects);
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
		ResourceLocation resourcelocation = Registry.POTION.getKey(potion);
		if (potion == Potions.EMPTY) {
			fs.removeChildTag("Potion");
			return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
		}
		fs.getOrCreateTag()
			.putString("Potion", resourcelocation.toString());
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<MobEffectInstance> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundTag compoundnbt = fs.getOrCreateTag();
		ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (MobEffectInstance effectinstance : customEffects)
			listnbt.add(effectinstance.save(new CompoundTag()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return new FluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
	}

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}

	// TODO: PORT
	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			int color = PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
			return color;
		}

		@Override
		public Component getDisplayName(FluidStack stack) {
			return new TranslatableComponent(getTranslationKey(stack));
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			ItemLike itemFromBottleType =
					PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
			return PotionUtils.getPotion(tag)
					.getName(itemFromBottleType.asItem()
							.getDescriptionId() + ".effect.");
		}

	}

}
