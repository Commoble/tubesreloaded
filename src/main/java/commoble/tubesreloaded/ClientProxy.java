package commoble.tubesreloaded;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class ClientProxy
{
	private static ClientProxy instance = new ClientProxy();
	
	private static final Set<BlockPos> NO_TUBES = ImmutableSet.of();
	
	private boolean isHoldingSprint = false;
	
	private Map<ChunkPos, Set<BlockPos>> tubesInChunk = new HashMap<>();
	
	public static void reset()
	{
		instance = new ClientProxy();
	}
	
	public static boolean getWasSprinting()
	{
		return instance.isHoldingSprint;
	}
	
	public static void setIsSprintingAndNotifyServer(boolean isSprinting)
	{
		// mark the capability on the client and send a packet to the server to do the same
		instance.isHoldingSprint = isSprinting;
		TubesReloaded.CHANNEL.sendToServer(new IsWasSprintPacket(isSprinting));
	}
	
	public static void updateTubesInChunk(ChunkPos pos, Set<BlockPos> tubes)
	{
		instance.tubesInChunk.put(pos, tubes);
	}
	
	public static Set<BlockPos> getTubesInChunk(ChunkPos pos)
	{
		return instance.tubesInChunk.getOrDefault(pos, NO_TUBES);
	}
}
