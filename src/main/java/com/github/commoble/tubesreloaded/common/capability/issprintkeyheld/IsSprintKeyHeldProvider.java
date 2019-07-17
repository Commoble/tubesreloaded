package com.github.commoble.tubesreloaded.common.capability.issprintkeyheld;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class IsSprintKeyHeldProvider implements ICapabilityProvider	
{
	@CapabilityInject(IIsSprintKeyHeldCapability.class)
	public static final Capability<IIsSprintKeyHeldCapability> IS_SPRINT_KEY_HELD_CAP = null;
	
	private IIsSprintKeyHeldCapability instance = IS_SPRINT_KEY_HELD_CAP.getDefaultInstance();
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		// TODO Auto-generated method stub
		if (cap == IS_SPRINT_KEY_HELD_CAP)
		{
			return LazyOptional.of(() -> instance).cast();
		}
		else
		{
			return LazyOptional.empty();
		}
	}

}
