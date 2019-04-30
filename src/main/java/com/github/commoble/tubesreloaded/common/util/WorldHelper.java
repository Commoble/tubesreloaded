package com.github.commoble.tubesreloaded.common.util;

import java.util.ArrayList;
import java.util.List;

import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldHelper
{
	public static List<TileEntityBrassTube> getTubesAdjacentTo(World world, BlockPos pos)
	{
		List<TileEntityBrassTube> tes = new ArrayList<TileEntityBrassTube>(6);
		for (EnumFacing face : EnumFacing.values())
		{
			BlockPos checkPos = pos.offset(face);
			TileEntity te = world.getTileEntity(checkPos);
			if (te instanceof TileEntityBrassTube)
			{
				tes.add((TileEntityBrassTube)te);
			}
		}
		
		return tes;
	}
}
