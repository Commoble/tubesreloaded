package commoble.tubesreloaded;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class IsWasSprintPacket implements CustomPacketPayload
{
	public static final ResourceLocation ID = new ResourceLocation(TubesReloaded.MODID, "is_was_sprint");
	private boolean isSprintHeld;
	
	public IsWasSprintPacket(boolean isSprintHeld)
	{
		this.isSprintHeld = isSprintHeld;
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeByte(this.isSprintHeld ? 1 : 0);
	}
	
	public static IsWasSprintPacket read(FriendlyByteBuf buf)
	{
		return new IsWasSprintPacket(buf.readByte() > 0);
	}
	
	public void handle(PlayPayloadContext context)
	{
		// PlayerData needs to be threadsafed, packet handling is done on worker threads, delegate to main thread
		context.workHandler().execute(() -> context.player().ifPresent(player -> PlayerData.setSprinting(player.getUUID(), this.isSprintHeld)));
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
