package commoble.tubesreloaded.blocks.tube;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class TubesInChunk implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
	public static final Capability<TubesInChunk> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	
	/** This codec serializes a list-like element **/
	public static final Codec<Set<BlockPos>> TUBE_SET_CODEC = BlockPos.CODEC.listOf().xmap(HashSet::new, ArrayList::new);
	/** half-codec that can be used in other places **/
	public static final MapCodec<Set<BlockPos>> FIELD_CODEC = TUBE_SET_CODEC.fieldOf("positions");
	/** This codec serializes a maplike element, its results can be cast to CompoundNBT**/
	public static final Codec<Set<BlockPos>> CODEC = FIELD_CODEC.codec();
	
	private final LazyOptional<TubesInChunk> holder = LazyOptional.of(() -> this);
	
	/** The positions in this set are world coordinates, not local-to-chunk coordinates **/
	private Set<BlockPos> positions = new HashSet<>();
	
	private final LevelChunk chunk; public LevelChunk getChunk() {return this.chunk;}
	
	public TubesInChunk(LevelChunk chunk)
	{
		this.chunk = chunk;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == CAPABILITY)
		{
			return CAPABILITY.orEmpty(cap, this.holder);
		}
		else
		{
			return LazyOptional.empty();
		}
	}

	public Set<BlockPos> getPositions()
	{
		return this.positions;
	}

	public void setPositions(Set<BlockPos> set)
	{
		this.positions = set;
		TubesReloaded.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(this::getChunk), new SyncTubesInChunkPacket(this.chunk.getPos(), set));
	}

	@Override
	public CompoundTag serializeNBT()
	{
		return CODEC.encodeStart(NbtOps.INSTANCE, this.getPositions())
			.result()
			.map(tag -> tag instanceof CompoundTag compound ? compound : null)
			.orElseGet(CompoundTag::new);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		CODEC.decode(NbtOps.INSTANCE, nbt).result().map(Pair::getFirst).ifPresent(this::setPositions);
	}
	
	public static Set<ChunkPos> getRelevantChunkPositionsNearPos(BlockPos pos)
	{
		double range = TubesReloaded.get().serverConfig().maxTubeConnectionRange().get();
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
	
	/**
	 * Gets the tubes in the chunk if the chunk is loaded, if called on the client uses the synced tube data
	 * @param level Level
	 * @param chunkPos ChunkPos
	 * @return Set of the tubes in the chunk, empty set if chunk not loaded
	 */
	public static Set<BlockPos> getTubesInChunkIfLoaded(LevelAccessor level, ChunkPos chunkPos)
	{
		if (level.isClientSide())
		{
			return ClientProxy.getTubesInChunk(chunkPos);
		}
		else if (level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) instanceof LevelChunk chunk)
		{
			return chunk.getCapability(TubesInChunk.CAPABILITY)
				.map(TubesInChunk::getPositions)
				.orElse(Set.of());
		}
		else
		{
			return Set.of();	
		}
	}
	
	public void onCapabilityInvalidated()
	{
		this.holder.invalidate();
	}

}
