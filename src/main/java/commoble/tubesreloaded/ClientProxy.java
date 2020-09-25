package commoble.tubesreloaded;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import commoble.tubesreloaded.network.IsWasSprintPacket;
import commoble.tubesreloaded.network.PacketHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.DistExecutor;

public class ClientProxy
{
	public static final Optional<ClientProxy> INSTANCE = DistExecutor.runForDist(
			() -> () -> Optional.of(new ClientProxy()), 
			() -> () -> Optional.empty());
	
	private static final Set<BlockPos> NO_TUBES = ImmutableSet.of();
	
	private boolean isHoldingSprint = false;
	
	private Map<ChunkPos, Set<BlockPos>> tubesInChunk = new HashMap<>();
	
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
	
	public void updateTubesInChunk(ChunkPos pos, Set<BlockPos> tubes)
	{
		this.tubesInChunk.put(pos, tubes);
	}
	
	public Set<BlockPos> getTubesInChunk(ChunkPos pos)
	{
		return this.tubesInChunk.getOrDefault(pos, NO_TUBES);
	}
}
