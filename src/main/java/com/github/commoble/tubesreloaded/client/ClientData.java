package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.network.IsWasSprintPacket;
import com.github.commoble.tubesreloaded.network.PacketHandler;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

public class ClientData
{
	public static final LazyOptional<ClientData> INSTANCE = DistExecutor.runForDist(
			() -> () -> LazyOptional.of(() -> new ClientData()), 
			() -> () -> LazyOptional.empty());
	
	public boolean isHoldingSprint = false;
	
	public boolean getWasSprinting()
	{
		return this.isHoldingSprint;
	}
	
	public void setIsSprintingAndNotifyServer(boolean isSprinting)
	{
		// mark the capability on the client and send a packet to the server to do the same
		this.isHoldingSprint = isSprinting;
		PacketHandler.INSTANCE.sendToServer(new IsWasSprintPacket(isSprinting));
	}
}
