package com.github.commoble.tubesreloaded.network;

import java.util.function.Supplier;

import com.github.commoble.tubesreloaded.common.capability.issprintkeyheld.IsSprintKeyHeldProvider;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class IsWasSprintPacket
{
	private boolean isSprintHeld;
	
	public IsWasSprintPacket(boolean isSprintHeld)
	{
		this.isSprintHeld = isSprintHeld;
	}
	
	public void encode(PacketBuffer buf)
	{
		buf.writeByte(isSprintHeld ? 1 : 0);
	}
	
	public static IsWasSprintPacket decode(PacketBuffer buf)
	{
		return new IsWasSprintPacket(buf.readByte() > 0);
	}
	
	public void whenThisPacketIsReceived(Supplier<NetworkEvent.Context> context)
	{
		ServerPlayerEntity player = context.get().getSender();
		if (player != null)
		{
			player.getCapability(IsSprintKeyHeldProvider.IS_SPRINT_KEY_HELD_CAP).ifPresent(cap -> cap.setIsSprintHeld(this.isSprintHeld));
		}
	}
}
