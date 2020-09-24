package commoble.tubesreloaded.blocks.tube;

import java.util.ArrayList;
import java.util.HashSet;

import commoble.tubesreloaded.util.NBTListHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
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
		
		private static final NBTListHelper<BlockPos> POS_LISTER = new NBTListHelper<>(
			POSITIONS,
			NBTUtil::writeBlockPos,
			NBTUtil::readBlockPos
			);
		
		@Override
		public INBT writeNBT(Capability<ITubesInChunk> capability, ITubesInChunk instance, Direction side)
		{
			return POS_LISTER.write(new ArrayList<>(instance.getPositions()), new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<ITubesInChunk> capability, ITubesInChunk instance, Direction side, INBT nbt)
		{
			if (nbt instanceof CompoundNBT)
			{
				instance.setPositions(new HashSet<>(POS_LISTER.read((CompoundNBT)nbt)));
			}
		}
		
	}
}
