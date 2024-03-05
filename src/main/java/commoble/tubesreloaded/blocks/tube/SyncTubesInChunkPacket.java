package commoble.tubesreloaded.blocks.tube;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.MiscCodecs;
import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class SyncTubesInChunkPacket implements CustomPacketPayload
{
	public static final ResourceLocation ID = new ResourceLocation(TubesReloaded.MODID, "sync_tubes_in_chunk");
	
	public static final Codec<SyncTubesInChunkPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MiscCodecs.COMPRESSED_CHUNK_POS.fieldOf("chunk").forGetter(SyncTubesInChunkPacket::getChunkPos),
			TubesInChunk.FIELD_CODEC.forGetter(SyncTubesInChunkPacket::getTubesInChunk)
		).apply(instance, SyncTubesInChunkPacket::new));
	
	public static final SyncTubesInChunkPacket BAD_PACKET = new SyncTubesInChunkPacket(new ChunkPos(ChunkPos.INVALID_CHUNK_POS), ImmutableSet.of());
	
	private final ChunkPos chunkPos;	public ChunkPos getChunkPos() {return this.chunkPos;}
	private final Set<BlockPos> tubesInChunk;	public Set<BlockPos> getTubesInChunk() {return this.tubesInChunk;}
	
	public SyncTubesInChunkPacket(ChunkPos chunkPos, Set<BlockPos> tubesInChunk)
	{
		this.chunkPos = chunkPos;
		this.tubesInChunk = tubesInChunk;
	}
	
	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeNbt((CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag()));
	}
	
	public static SyncTubesInChunkPacket read(FriendlyByteBuf buffer)
	{
		return CODEC.decode(NbtOps.INSTANCE, buffer.readNbt()).result().map(Pair::getFirst).orElse(BAD_PACKET);
	}
	
	public void handle(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> ClientProxy.updateTubesInChunk(this.chunkPos, this.tubesInChunk));
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
