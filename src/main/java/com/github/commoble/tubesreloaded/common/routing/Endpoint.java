package com.github.commoble.tubesreloaded.common.routing;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class Endpoint
{	
	public EnumSet<EnumFacing> relevantFaces = EnumSet.noneOf(EnumFacing.class);
	public BlockPos pos;	// TEs can become invalidated or replaced, so get new ones when needed
	
	public Endpoint(BlockPos tePos)
	{
		this.pos = tePos;
	}
	
	// create an Endpoint such that its relevant faces are those that
	// a) have an item handler capability, and
	// b) face a position in the tubeSet
	@Nonnull
	public static Endpoint createEndpoint(BlockPos tePos, World world, Set<BlockPos> tubeSet)
	{
		TileEntity te = world.getTileEntity(tePos);
		Endpoint endpoint = new Endpoint(tePos);
		
		if (te == null)
		{
			return endpoint; // empty endpoint
		}
		
		for (EnumFacing face : EnumFacing.values())
		{
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).isPresent())
			{
				if (tubeSet.contains(tePos.offset(face)))
				{
					endpoint.relevantFaces.add(face);
				}
			}
		}
		
		return endpoint;
	}
	
	
	
	
	
}
