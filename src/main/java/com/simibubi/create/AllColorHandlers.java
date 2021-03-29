package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

import com.simibubi.create.foundation.block.BlockVertexColorProvider;
import com.simibubi.create.foundation.block.render.ColoredVertexModel;

public class AllColorHandlers {

	private final Map<Block, BlockVertexColorProvider> coloredVertexBlocks = new HashMap<>();
	private final Map<Block, BlockColorProvider> coloredBlocks = new HashMap<>();
	private final Map<ItemConvertible, ItemColorProvider> coloredItems = new HashMap<>();

	//

	public static BlockColorProvider getGrassyBlock() {
		return new BlockColor(
			(state, world, pos, layer) -> pos != null && world != null ? BiomeColors.getGrassColor(world, pos)
				: GrassColors.getColor(0.5D, 1.0D));
	}

	public static ItemColorProvider getGrassyItem() {
		return new ItemColor((stack, layer) -> GrassColors.getColor(0.5D, 1.0D));
	}

	public static BlockColorProvider getRedstonePower() {
		return new BlockColor(
				(state, world, pos, layer) -> RedstoneWireBlock.getWireColor(pos != null && world != null ? state.get(Properties.POWER) : 0)
		);
	}

	//

	public void register(Block block, BlockColorProvider color) {
		coloredBlocks.put(block, color);
	}

	public void register(Block block, BlockVertexColorProvider color) {
		coloredVertexBlocks.put(block, color);
	}

	public void register(ItemConvertible item, ItemColorProvider color) {
		coloredItems.put(item, color);
	}

	public void init() {
		coloredBlocks.forEach((block, color) -> ColorProviderRegistry.BLOCK.register(color, block));
		coloredItems.forEach((item, color) -> ColorProviderRegistry.ITEM.register(color, item));
		coloredVertexBlocks.forEach((block, color) -> CreateClient.getCustomBlockModels()
			.register(() -> block, model -> new ColoredVertexModel(model, color)));
	}

	//

	private static class ItemColor implements ItemColorProvider {

		private Function function;

		@FunctionalInterface
		interface Function {
			int apply(ItemStack stack, int layer);
		}

		public ItemColor(Function function) {
			this.function = function;
		}

		@Override
		public int getColor(ItemStack stack, int layer) {
			return function.apply(stack, layer);
		}

	}

	private static class BlockColor implements BlockColorProvider {

		private com.simibubi.create.AllColorHandlers.BlockColor.Function function;

		@FunctionalInterface
		interface Function {
			int apply(BlockState state, BlockRenderView world, BlockPos pos, int layer);
		}

		public BlockColor(com.simibubi.create.AllColorHandlers.BlockColor.Function function) {
			this.function = function;
		}

		@Override
		public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int layer) {
			return function.apply(state, world, pos, layer);
		}

	}

}
