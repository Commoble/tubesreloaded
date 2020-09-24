package commoble.tubesreloaded.blocks.tube;

import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import commoble.databuddy.codec.SetCodecHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class TubesInChunkCapability
{
	@CapabilityInject(ITubesInChunk.class)
	public static Capability<ITubesInChunk> INSTANCE = null;
	
	public static class Storage implements Capability.IStorage<ITubesInChunk>
	{
		public static final String POSITIONS = "positions";
		
		private static final Codec<Set<BlockPos>> CODEC = SetCodecHelper.makeSetCodec(BlockPos.CODEC);
		
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
