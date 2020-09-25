package commoble.tubesreloaded.blocks.tube;

import java.util.Set;
import java.util.function.UnaryOperator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.databuddy.codec.SetCodecHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/** Don't call INSTANCE.getDefaultInstance(), default instance isn't supported **/
public class TubesInChunkCapability
{
	@CapabilityInject(ITubesInChunk.class)
	public static Capability<ITubesInChunk> INSTANCE = null;
	
	/** This codec serializes a list-like element **/
	public static final Codec<Set<BlockPos>> TUBE_SET_CODEC = SetCodecHelper.makeSetCodec(BlockPos.CODEC);
	
	public static class Storage implements Capability.IStorage<ITubesInChunk>
	{		
		/** Wrap in a record coded -- this serializes to a map-like element, so it will convert to a CompoundNBT **/
		private static final Codec<Set<BlockPos>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				TUBE_SET_CODEC.fieldOf("positions").forGetter(UnaryOperator.identity())
			).apply(instance, UnaryOperator.identity()));
		
		@Override
		public INBT writeNBT(Capability<ITubesInChunk> capability, ITubesInChunk instance, Direction side)
		{
			return CODEC.encodeStart(NBTDynamicOps.INSTANCE, instance.getPositions()).result().orElse(new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<ITubesInChunk> capability, ITubesInChunk instance, Direction side, INBT nbt)
		{
			CODEC.decode(NBTDynamicOps.INSTANCE, nbt).result().map(Pair::getFirst).ifPresent(instance::setPositions);
		}
		
	}
}
