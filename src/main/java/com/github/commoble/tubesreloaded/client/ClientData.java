package com.github.commoble.tubesreloaded.client;

import javax.annotation.Nonnull;

import com.github.commoble.tubesreloaded.common.capability.issprintkeyheld.IIsSprintKeyHeldCapability;
import com.github.commoble.tubesreloaded.common.capability.issprintkeyheld.IsSprintKeyHeldProvider;
import com.github.commoble.tubesreloaded.network.IsWasSprintPacket;
import com.github.commoble.tubesreloaded.network.PacketHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

public class ClientData
{
	public static final LazyOptional<ClientData> INSTANCE = DistExecutor.runForDist(
			() -> () -> LazyOptional.of(() -> new ClientData()), 
			() -> () -> LazyOptional.empty());
	
	private LazyOptional<IIsSprintKeyHeldCapability> possibleSprintCap = LazyOptional.empty();
	
	private LazyOptional<IIsSprintKeyHeldCapability> getSprintCap(@Nonnull PlayerEntity player)
	{
		if (!this.possibleSprintCap.isPresent())
		{
			this.possibleSprintCap = player.getCapability(IsSprintKeyHeldProvider.IS_SPRINT_KEY_HELD_CAP);
		}
		return this.possibleSprintCap;
	}
	
	public boolean getWasSprinting(@Nonnull PlayerEntity player)
	{
		return this.getSprintCap(player).map(cap -> cap.getIsSprintHeld()).orElse(false);
	}
	
	public void setIsSprintingAndNotifyServer(@Nonnull PlayerEntity player, boolean isSprinting)
	{
		// mark the capability on the client and send a packet to the server to do the same
		this.getSprintCap(player).ifPresent(cap -> cap.setIsSprintHeld(isSprinting));
		PacketHandler.INSTANCE.sendToServer(new IsWasSprintPacket(isSprinting));
	}
}
