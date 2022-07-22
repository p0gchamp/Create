package com.simibubi.create.content.logistics.block.display.source;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.FluidFormatter;
import com.simibubi.create.foundation.utility.LongAttached;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

public class FluidListDisplaySource extends ValueListDisplaySource {


	@Override
	protected Stream<LongAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return Stream.empty();

		TankManipulationBehaviour tankManipulationBehaviour = cote.getBehaviour(TankManipulationBehaviour.OBSERVE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		Storage<FluidVariant> handler = tankManipulationBehaviour.getInventory();

		if (handler == null)
			return Stream.empty();


		Map<Fluid, Long> fluids = new HashMap<>();
		Map<Fluid, FluidStack> fluidNames = new HashMap<>();

		try (Transaction t = TransferUtil.getTransaction()) {
			for (StorageView<FluidVariant> view : handler.iterable(t)) {
				if (view.isResourceBlank())
					continue;
				FluidStack stack = new FluidStack(view);
				if (!filteringBehaviour.test(stack))
					continue;

				fluids.merge(stack.getFluid(), stack.getAmount(), Long::sum);
				fluidNames.putIfAbsent(stack.getFluid(), stack);
			}
		}

		return fluids.entrySet()
				.stream()
				.sorted(Comparator.<Map.Entry<Fluid, Long>>comparingDouble(value -> value.getValue()).reversed())
				.limit(maxRows)
				.map(entry -> LongAttached.with(
						entry.getValue(),
						FluidVariantAttributes.getName(fluidNames.get(entry.getKey()).getType()).copy())
				);
	}

	@Override
	protected List<MutableComponent> createComponentsFromEntry(DisplayLinkContext context, LongAttached<MutableComponent> entry) {
		long amount = entry.getFirst();
		MutableComponent name = entry.getSecond().append(WHITESPACE);

		Couple<MutableComponent> formatted = FluidFormatter.asComponents(amount, shortenNumbers(context));

		return List.of(formatted.getFirst(), formatted.getSecond(), name);
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayTileEntity flapDisplay, FlapDisplayLayout layout) {
		Integer max = ((MutableInt) context.flapDisplayContext).getValue();
		boolean shorten = shortenNumbers(context);
		int length = FluidFormatter.asString(max, shorten).length();
		String layoutKey = "FluidList_" + length;

		if (layout.isLayout(layoutKey))
			return;

		int maxCharCount = flapDisplay.getMaxCharCount(1);
		int numberLength = Math.min(maxCharCount, Math.max(3, length - 2));
		int nameLength = Math.max(maxCharCount - numberLength - 2, 0);

		FlapDisplaySection value = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * numberLength, "number", false, false).rightAligned();
		FlapDisplaySection unit = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * 2, "fluid_units", true, true);
		FlapDisplaySection name = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * nameLength, "alphabet", false, false);

		layout.configure(layoutKey, List.of(value, unit, name));
	}

	@Override
	protected String getTranslationKey() {
		return "list_fluids";
	}

	@Override
	protected boolean valueFirst() {
		return false;
	}
}
