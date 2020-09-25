package commoble.tubesreloaded.blocks.tube;

import java.util.HashSet;
import java.util.Set;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.network.PacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

public class TubesInChunk implements ITubesInChunk, ICapabilityProvider, INBTSerializable<CompoundNBT>
{	
	private final LazyOptional<ITubesInChunk> holder = LazyOptional.of(() -> this);
	
	/** The positions in this set are world coordinates, not local-to-chunk coordinates **/
	private Set<BlockPos> positions = new HashSet<>();
	
	private final Chunk chunk; public Chunk getChunk() {return this.chunk;}
	
	public TubesInChunk(Chunk chunk)
	{
		this.chunk = chunk;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == TubesInChunkCapability.INSTANCE)
		{
			return TubesInChunkCapability.INSTANCE.orEmpty(cap, this.holder);
		}
		else
		{
			return LazyOptional.empty();
		}
	}

	@Override
	public Set<BlockPos> getPositions()
	{
		return this.positions;
	}

	@Override
	public void setPositions(Set<BlockPos> set)
	{
		this.positions = set;
		PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(this::getChunk), new SyncTubesInChunkPacket(this.chunk.getPos(), set));
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT)TubesInChunkCapability.INSTANCE.getStorage().writeNBT(TubesInChunkCapability.INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		TubesInChunkCapability.INSTANCE.getStorage().readNBT(TubesInChunkCapability.INSTANCE, this, null, nbt);
	}
	
	public static Set<ChunkPos> getRelevantChunkPositionsNearPos(BlockPos pos)
	{
		double range = TubesReloaded.serverConfig.max_remote_tube_connection_range.get();
		ChunkPos chunkPos = new ChunkPos(pos);
		int chunkRange = (int) Math.ceil(range/16D);
		Set<ChunkPos> set = new HashSet<>();
		for (int xOff = -chunkRange; xOff <= chunkRange; xOff++)
		{
			for (int zOff = -chunkRange; zOff <= chunkRange; zOff++)
			{
				set.add(new ChunkPos(chunkPos.x + xOff, chunkPos.z + zOff));
			}
		}
		
		return set;
	}
	
	public void onCapabilityInvalidated()
	{
		this.holder.invalidate();
	}

}
