package com.github.commoble.tubesreloaded.common.blocks.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.blocks.shunt.ShuntBlock;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class FilterTileEntity extends TileEntity
{
	public ItemStack filterStack = ItemStack.EMPTY;
	public FilterItemHandler handler = new FilterItemHandler(this);
	
	public FilterTileEntity()
	{
		super(TileEntityRegistrar.TE_TYPE_FILTER);
	}
	
	public boolean canItemPassThroughFilter(ItemStack stack)
	{
		if (this.filterStack.isEmpty())
		{
			return true;
		}
		
		return this.filterStack.getItem().equals(stack.getItem());
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			Direction output_dir = this.getBlockState().get(ShuntBlock.FACING);
			if (side != output_dir)
			{
				return LazyOptional.of(() -> this.handler).cast();
			}
		}
		return super.getCapability(cap, side);
	}
}
