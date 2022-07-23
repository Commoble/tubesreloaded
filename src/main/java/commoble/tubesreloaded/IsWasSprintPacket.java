package commoble.tubesreloaded;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

public class IsWasSprintPacket
{
	private boolean isSprintHeld;
	
	public IsWasSprintPacket(boolean isSprintHeld)
	{
		this.isSprintHeld = isSprintHeld;
	}
	
	public void write(FriendlyByteBuf buf)
	{
		buf.writeByte(this.isSprintHeld ? 1 : 0);
	}
	
	public static IsWasSprintPacket read(FriendlyByteBuf buf)
	{
		return new IsWasSprintPacket(buf.readByte() > 0);
	}
	
	public void handle(Supplier<NetworkEvent.Context> contextGetter)
	{
		NetworkEvent.Context context = contextGetter.get();
		// PlayerData needs to be threadsafed, packet handling is done on worker threads, delegate to main thread
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null)
			{
				PlayerData.setSprinting(player.getUUID(), this.isSprintHeld);
			}
		});
	}
}
