package com.github.commoble.tubesreloaded.common.blocks.filter;

import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

public class OsmosisFilterTileEntity extends FilterTileEntity implements ITickableTileEntity
{
	public boolean isDormant = false; // if true, will check the inventory it faces every tick
	public long transferHash;

	public OsmosisFilterTileEntity()
	{
		super(TileEntityRegistrar.TE_TYPE_OSMOSIS_FILTER);
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.transferHash = this.pos.hashCode();
	}

	@Override
	public void tick()
	{
		if (!this.isDormant && (this.world.getDayTime() + this.transferHash) % ConfigValues.osmosis_filter_transfer_rate == 0
			&& !this.getBlockState().get(OsmosisFilterBlock.ENABLED))
		{
			this.isDormant = true;
			Direction filterOutputDirection = this.getBlockState().get(FilterBlock.FACING);
			Direction filterInputDirection = filterOutputDirection.getOpposite();
			WorldHelper.getTEItemHandlerAt(this.world, this.pos.offset(filterInputDirection), filterOutputDirection).map(inventory -> this.getFirstValidItem(inventory))
				.ifPresent(stack -> {
					if (stack.getCount() > 0)
					{
						this.shuntingHandler.insertItem(0, stack, false);
						this.isDormant = false;
					}
				});
		}
	}

	public ItemStack getFirstValidItem(IItemHandler inventory)
	{
		return IntStream.range(0, inventory.getSlots())
			.mapToObj(slotIndex -> Pair.of(slotIndex, inventory.getStackInSlot(slotIndex)))
			.filter(slot -> this.canItemPassThroughFilter(slot.getRight()))
			.findFirst()
			.map(slot -> inventory.extractItem(slot.getLeft(), 1, false))
			.orElse(ItemStack.EMPTY);
	}
}
