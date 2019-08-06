package com.github.commoble.tubesreloaded.common;

import java.util.Optional;

import com.github.commoble.tubesreloaded.network.IsWasSprintPacket;
import com.github.commoble.tubesreloaded.network.PacketHandler;

import net.minecraftforge.fml.DistExecutor;

public class ClientProxy
{
	public static final Optional<ClientProxy> INSTANCE = DistExecutor.runForDist(
			() -> () -> Optional.of(new ClientProxy()), 
			() -> () -> Optional.empty());
	
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
