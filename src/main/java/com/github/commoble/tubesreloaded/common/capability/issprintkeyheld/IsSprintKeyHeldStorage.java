package com.github.commoble.tubesreloaded.common.capability.issprintkeyheld;

import javax.annotation.Nullable;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class IsSprintKeyHeldStorage implements Capability.IStorage<IIsSprintKeyHeldCapability>
{
	// we don't store anything
	@Nullable
	public INBT writeNBT(Capability<IIsSprintKeyHeldCapability> capability, IIsSprintKeyHeldCapability instance,
			Direction side)
	{
		return null;
	}

	// we don't store anything
	public void readNBT(Capability<IIsSprintKeyHeldCapability> capability, IIsSprintKeyHeldCapability instance, Direction side, INBT nbt)
	{

	}
}
