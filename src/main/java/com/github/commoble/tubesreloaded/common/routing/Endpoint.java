package com.github.commoble.tubesreloaded.common.routing;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class Endpoint
{	
	public final BlockPos pos;	// TEs can become invalidated or replaced, so get new ones when needed (not sure why this comment is here)
	public final EnumFacing face;	// the face of the block at this blockpos that represents the endpoint
	
	public Endpoint(BlockPos tePos, EnumFacing blockFace)
	{
		this.pos = tePos;
		this.face = blockFace;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)	// same instance, must be equal
		{
			return true;
		}
		else if (other instanceof Endpoint)
		{	// if other object is an endpoint,
			// this is equivalent to the other endpoint if and only if
			// both blockpos are equivalent and both endpoints are equivalent
			Endpoint otherEndpoint = (Endpoint) other;
			return this.pos.equals(otherEndpoint.pos) && this.face.equals(otherEndpoint.face);
		}
		else
		{
			return false;	// not an endpoint, can't be equal
		}
	}
	
	@Override
	public int hashCode()
	{
		return this.pos.hashCode() ^ this.face.hashCode();
	}
	
	@Override
	public String toString()
	{
		return this.pos + ";    " + this.face;
	}
	
	
}
