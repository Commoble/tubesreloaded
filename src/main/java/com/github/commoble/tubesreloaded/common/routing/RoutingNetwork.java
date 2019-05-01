package com.github.commoble.tubesreloaded.common.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RoutingNetwork
{
	public Set<BlockPos> tubes = new HashSet<BlockPos>();	// set of the tubes that make up the network interior
	public Map<BlockPos, Endpoint> endpoints = new HashMap<BlockPos, Endpoint>();	// set of the non-tube TEs at the edges of the network
	
	public static RoutingNetwork buildNetworkFrom(BlockPos pos, World world)
	{
		HashSet<BlockPos> visited = new HashSet<BlockPos>();
		HashSet<BlockPos> potentialEndpoints = new HashSet<BlockPos>();
		RoutingNetwork network = new RoutingNetwork();
		recursivelyBuildNetworkFrom(pos, world, network, visited, potentialEndpoints);
		// we now have a set of tubes and a set of potential endpoints
		// narrow down the endpoint TEs to useable ones
		for (BlockPos endPos : potentialEndpoints)
		{
			Endpoint point = Endpoint.createEndpoint(endPos, world, network.tubes);
			if (!point.relevantFaces.isEmpty())	// if the created endpoint can touch this network's tubes
			{	// add it to the network
				network.endpoints.put(endPos, point);
			}
		}
		
		
		return network;
	}
	
	private static void recursivelyBuildNetworkFrom(BlockPos pos, World world, RoutingNetwork network, HashSet<BlockPos> visited, HashSet<BlockPos> potentialEndpoints)
	{
		visited.add(pos);
		
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBrassTube)
		{
			network.tubes.add(pos);
			// build further from tubes
			for (EnumFacing face : EnumFacing.values())
			{
				BlockPos checkPos = pos.offset(face);
				if (!visited.contains(checkPos))
				{
					recursivelyBuildNetworkFrom(checkPos, world, network, visited, potentialEndpoints);
				}
			}
		}
		else if (te != null) // TE here but TE isn't a tube
		{
			// keep track of it for now and reconsider it later
			potentialEndpoints.add(pos);
			return;	// don't look further from here though
		}
		else
		{
			return;	// don't look further
		}
	}
}
