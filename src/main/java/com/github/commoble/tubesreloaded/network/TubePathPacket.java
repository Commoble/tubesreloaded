package com.github.commoble.tubesreloaded.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TubePathPacket
{
	public TubePathPacket()
	{
		
	}
	
	/** Write the contents of this packet into a PacketBuffer **/
	public void encode(PacketBuffer buf)
	{
		
	}
	
	/** Read the contents of a PacketBuffer into a new TubePathPacket instance **/
	public static TubePathPacket decode(PacketBuffer buf)
	{
		return new TubePathPacket();
	}
	
	public static void handle(TubePathPacket packet, Supplier<NetworkEvent.Context> context)
	{
		
	}
}
