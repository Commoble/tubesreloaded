package com.github.commoble.tubesreloaded.common.blocks.shunt;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class ShuntTileEntity extends TileEntity
{

	public ShuntTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}

	public ShuntTileEntity()
	{
		this(TileEntityRegistrar.TE_TYPE_SHUNT);
	}
}
