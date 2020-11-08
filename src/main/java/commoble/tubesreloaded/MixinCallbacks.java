package commoble.tubesreloaded;

import commoble.tubesreloaded.blocks.tube.SyncTubesInChunkPacket;
import commoble.tubesreloaded.blocks.tube.TubesInChunkCapability;
import commoble.tubesreloaded.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

public class MixinCallbacks
{
	// sync redwire post positions to clients when a chunk needs to be loaded on the client
	public static void afterSendChunkData(ServerPlayerEntity player, Chunk chunk)
	{
		ChunkPos pos = chunk.getPos();
		chunk.getCapability(TubesInChunkCapability.INSTANCE).ifPresent(cap -> 
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new SyncTubesInChunkPacket(pos, cap.getPositions()))
		);
	}
}
