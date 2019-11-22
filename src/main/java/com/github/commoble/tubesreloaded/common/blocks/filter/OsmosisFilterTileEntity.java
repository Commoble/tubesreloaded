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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.items.IItemHandler;

public class OsmosisFilterTileEntity extends FilterTileEntity implements ITickableTileEntity
{
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
		if (!this.world.isRemote)
		{
			if ((this.world.getDayTime() + this.transferHash) % ConfigValues.osmosis_filter_transfer_rate == 0
				&& this.getBlockState().get(OsmosisFilterBlock.TRANSFERRING_ITEMS))
			{
				Direction filterOutputDirection = this.getBlockState().get(FilterBlock.FACING);
				Direction filterInputDirection = filterOutputDirection.getOpposite();
				boolean transferredItemThisTick = WorldHelper.getTEItemHandlerAt(this.world, this.pos.offset(filterInputDirection), filterOutputDirection)
					.map(inventory -> this.getFirstValidItem(inventory))
					.map(stack -> this.attemptExtractionAndReturnSuccess(stack))
					.orElse(false);
				if (!transferredItemThisTick)
				{	// set dormant if no items were found
					this.world.setBlockState(this.pos, this.getBlockState().with(OsmosisFilterBlock.TRANSFERRING_ITEMS, false));
				}
				else
				{
					this.world.playSound(null, this.pos, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS,
						this.world.rand.nextFloat()*0.1F, this.world.rand.nextFloat());//this.world.rand.nextFloat()*0.01f + 0.005f);
				}
				
			}
		}
	}

	private boolean attemptExtractionAndReturnSuccess(ItemStack stack)
	{
		if (stack.getCount() > 0)
		{
			this.shuntingHandler.insertItem(0, stack, false);
			return true;
		}
		return false;
	}

	public ItemStack getFirstValidItem(IItemHandler inventory)
	{
		return IntStream.range(0, inventory.getSlots()).mapToObj(slotIndex -> Pair.of(slotIndex, inventory.getStackInSlot(slotIndex)))
			.filter(slot -> this.canItemPassThroughFilter(slot.getRight())).findFirst().map(slot -> inventory.extractItem(slot.getLeft(), 1, false)).orElse(ItemStack.EMPTY);
	}
}
