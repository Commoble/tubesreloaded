package commoble.tubesreloaded.blocks.tube;

import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.databuddy.codec.ExtraCodecs;
import commoble.tubesreloaded.ClientProxy;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncTubesInChunkPacket
{
	public static final Codec<SyncTubesInChunkPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.COMPRESSED_CHUNK_POS.fieldOf("chunk").forGetter(SyncTubesInChunkPacket::getChunkPos),
			TubesInChunkCapability.TUBE_SET_CODEC.fieldOf("tubes").forGetter(SyncTubesInChunkPacket::getTubesInChunk)
		).apply(instance, SyncTubesInChunkPacket::new));
	
	public static final SyncTubesInChunkPacket BAD_PACKET = new SyncTubesInChunkPacket(new ChunkPos(ChunkPos.SENTINEL), ImmutableSet.of());
	
	private final ChunkPos chunkPos;	public ChunkPos getChunkPos() {return this.chunkPos;}
	private final Set<BlockPos> tubesInChunk;	public Set<BlockPos> getTubesInChunk() {return this.tubesInChunk;}
	
	public SyncTubesInChunkPacket(ChunkPos chunkPos, Set<BlockPos> tubesInChunk)
	{
		this.chunkPos = chunkPos;
		this.tubesInChunk = tubesInChunk;
	}
	
	public void write(PacketBuffer buffer)
	{
		buffer.writeCompoundTag((CompoundNBT)CODEC.encodeStart(NBTDynamicOps.INSTANCE, this).result().orElse(new CompoundNBT()));
	}
	
	public static SyncTubesInChunkPacket read(PacketBuffer buffer)
	{
		return CODEC.decode(NBTDynamicOps.INSTANCE, buffer.readCompoundTag()).result().map(Pair::getFirst).orElse(BAD_PACKET);
	}
	
	public void handle(Supplier<NetworkEvent.Context> contextGetter)
	{
		NetworkEvent.Context context = contextGetter.get();
		ClientProxy.INSTANCE.ifPresent(proxy -> contextGetter.get().enqueueWork(() -> proxy.updateTubesInChunk(this.chunkPos,this.tubesInChunk)));
		context.setPacketHandled(true);
	}
}
