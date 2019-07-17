package com.github.commoble.tubesreloaded.common.capability.issprintkeyheld;

public class IsSprintKeyHeldCapability implements IIsSprintKeyHeldCapability
{
	private boolean isSprintHeld;
	
	@Override
	public boolean getIsSprintHeld()
	{
		return this.isSprintHeld;
	}
	
	@Override
	public void setIsSprintHeld(boolean isSprintHeld)
	{
		this.isSprintHeld = isSprintHeld;
	}
}
