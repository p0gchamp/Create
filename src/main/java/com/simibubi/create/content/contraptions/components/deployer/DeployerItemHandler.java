package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import com.simibubi.create.foundation.utility.fabric.ListEntryConsumer;
import com.simibubi.create.foundation.utility.fabric.ListEntrySupplier;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DeployerItemHandler extends SnapshotParticipant<Unit> implements Storage<ItemVariant> {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerTileEntity te) {
		this.te = te;
		this.player = te.player;
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (te.getLevel().isClientSide)
			return;
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int maxInsert = Math.min((int) maxAmount, resource.getItem().getMaxStackSize());
		ItemStack stack = resource.toStack(maxInsert);
		if (!isItemValid(stack))
			return 0;

		ItemStack held = getHeld();
		if (held.isEmpty()) {
			te.snapshotParticipant.updateSnapshots(transaction);
			set(stack);
			return maxInsert;
		}

		if (!ItemHandlerHelper.canItemStacksStack(held, stack))
			return 0;

		int space = held.getMaxStackSize() - held.getCount();
		if (space == 0)
			return 0;

		int toInsert = Math.min(maxInsert, space);
		ItemStack newStack = held.copy();
		newStack.grow(toInsert);
		te.snapshotParticipant.updateSnapshots(transaction);
		set(newStack);
		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = extractFromOverflow(resource, maxAmount, transaction);
		if (extracted == maxAmount)
			return extracted;
		extracted += extractFromHeld(resource, maxAmount - extracted, transaction);
		return extracted;
	}

	/**
	 * @return number of items extracted from the TE's overflow slots
	 */
	public long extractFromOverflow(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = 0;
		for (int i = 0; i < te.overflowItems.size(); i++) {
			ItemStack itemStack = te.overflowItems.get(i);
			if (itemStack.isEmpty())
				continue;
			if (!resource.matches(itemStack))
				continue;
			int toExtract = (int) Math.min(maxAmount - extracted, itemStack.getCount());
			if (extracted == 0)
				te.snapshotParticipant.updateSnapshots(transaction);
			extracted += toExtract;
			ItemStack newStack = itemStack.copy();
			newStack.shrink(toExtract);
			te.overflowItems.set(i, newStack);
		}
		return extracted;
	}

	/**
	 * @return the number of items extracted from the TE's held stack
	 */
	public long extractFromHeld(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack held = getHeld();
		if (held.isEmpty() || !resource.matches(held))
			return 0;
		if (!te.filtering.getFilter().isEmpty() && te.filtering.test(held))
			return 0;
		int toExtract = (int) Math.min(maxAmount, held.getCount());
		te.snapshotParticipant.updateSnapshots(transaction);
		ItemStack newStack = held.copy();
		newStack.shrink(toExtract);
		set(newStack);
		return toExtract;
	}

	public boolean isItemValid(ItemStack stack) {
		FilteringBehaviour filteringBehaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	protected Unit createSnapshot() {
		return Unit.INSTANCE;
	}

	@Override
	protected void readSnapshot(Unit snapshot) {
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		te.setChanged();
		te.sendData();
	}

	@Override
	public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return new DeployerItemHandlerIterator(transaction);
	}

	public class DeployerItemHandlerIterator implements Iterator<StorageView<ItemVariant>> {
		private boolean open = true;
		private int index; // -1 means held

		public DeployerItemHandlerIterator(TransactionContext transaction) {
			transaction.addCloseCallback((t, r) -> open = false);
			this.index = te.overflowItems.size() != 0 ? 0 : -1;
		}

		@Override
		public boolean hasNext() {
			return open && index < te.overflowItems.size();
		}

		@Override
		public StorageView<ItemVariant> next() {
			Supplier<ItemStack> heldGetter = new ListEntrySupplier<>(index, te.overflowItems);
			Consumer<ItemStack> heldSetter = new ListEntryConsumer<>(index, te.overflowItems);
			Predicate<ItemStack> mayExtract = stack -> true;
			if (index == -1) {
				heldGetter = player::getMainHandItem;
				heldSetter = s -> player.setItemInHand(InteractionHand.MAIN_HAND, s);
				mayExtract = s -> te.filtering.getFilter().isEmpty() || !te.filtering.test(s);
				index = te.overflowItems.size(); // hasNext will be false now
			}
			index++;
			if (index == te.overflowItems.size())
				index = -1;
			return new DeployerSlotView(heldGetter, heldSetter, mayExtract);
		}

	}

	public class DeployerSlotView extends SnapshotParticipant<Unit> implements StorageView<ItemVariant> {
		public final Supplier<ItemStack> heldGetter;
		public final Consumer<ItemStack> heldSetter;
		public final Predicate<ItemStack> mayExtract;

		public DeployerSlotView(Supplier<ItemStack> heldGetter, Consumer<ItemStack> heldSetter, Predicate<ItemStack> mayExtract) {
			this.heldGetter = heldGetter;
			this.heldSetter = heldSetter;
			this.mayExtract = mayExtract;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack stack = getStack();
			if (stack.isEmpty() || !resource.matches(stack) || !mayExtract.test(stack))
				return 0;
			int toExtract = (int) Math.min(maxAmount, stack.getCount());
			updateSnapshots(transaction);
			ItemStack newStack = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toExtract);
			heldSetter.accept(newStack);
			return toExtract;
		}

		@Override
		public boolean isResourceBlank() {
			return getResource().isBlank();
		}

		@Override
		public ItemVariant getResource() {
			ItemStack stack = getStack();
			return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
		}

		@Override
		public long getAmount() {
			return getStack().getCount();
		}

		@Override
		public long getCapacity() {
			return getStack().getMaxStackSize();
		}

		public ItemStack getStack() {
			return heldGetter.get();
		}

		@Override
		public void updateSnapshots(TransactionContext transaction) {
			super.updateSnapshots(transaction);
			te.snapshotParticipant.updateSnapshots(transaction);
		}

		@Override
		protected Unit createSnapshot() {
			return Unit.INSTANCE;
		}

		@Override
		protected void readSnapshot(Unit snapshot) {
		}

		@Override
		protected void onFinalCommit() {
			super.onFinalCommit();
			DeployerItemHandler.this.onFinalCommit();
		}
	}
}
