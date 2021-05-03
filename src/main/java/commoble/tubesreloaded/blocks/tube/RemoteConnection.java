package commoble.tubesreloaded.blocks.tube;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.databuddy.codec.ExtraCodecs;
import commoble.tubesreloaded.util.NestedBoundingBox;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class RemoteConnection
{	
	public final Direction toSide;
	public final BlockPos toPos;
	/** Every connection is stored inside both tubes, but only the primary connection will be rendered **/
	public final boolean isPrimary;
	private final BlockPos fromPos;
	private NestedBoundingBox box;
	
	public RemoteConnection(Direction fromSide, Direction toSide, BlockPos fromPos, BlockPos toPos, boolean isPrimary)
	{
		this.toSide = toSide;
		this.toPos = toPos;
		this.isPrimary = isPrimary;
		this.fromPos = fromPos;
	}
	
	public NestedBoundingBox getBox()
	{
		if (this.box == null)
			this.box = getNestedBoundingBoxForConnectedPos(this.fromPos, this.toPos);
		return this.box;
	}
	
	public Storage toStorage()
	{
		return new Storage(this.toSide, this.toPos, this.isPrimary);
	}
	
	public static RemoteConnection fromStorage(Storage storage, Direction fromSide, BlockPos fromPos)
	{
		return new RemoteConnection(fromSide, storage.toSide, fromPos, storage.toPos, storage.isPrimary);
	}
	
	private static NestedBoundingBox getNestedBoundingBoxForConnectedPos(BlockPos from, BlockPos to)
	{
		Vector3d thisVec = TubeTileEntity.getCenter(from);
		Vector3d otherVec = TubeTileEntity.getCenter(to);
		boolean otherHigher = otherVec.y > thisVec.y;
		Vector3d higherVec = otherHigher ? otherVec : thisVec;
		Vector3d lowerVec = otherHigher ? thisVec : otherVec;
		Vector3d[] points = RaytraceHelper.getInterpolatedPoints(lowerVec, higherVec);
		int segmentCount = points.length - 1;
		AxisAlignedBB[] boxes = new AxisAlignedBB[segmentCount];
		for (int i=0; i<segmentCount; i++)
		{
			boxes[i] = new AxisAlignedBB(points[i], points[i+1]);
		}
		return NestedBoundingBox.fromAABBs(boxes);
	}
	
	public static class Storage
	{
		public final Direction toSide;
		public final BlockPos toPos;
		public final boolean isPrimary;
		
		public static final Codec<Storage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ExtraCodecs.DIRECTION.fieldOf("toSide").forGetter(storage -> storage.toSide),
				BlockPos.CODEC.fieldOf("toPos").forGetter(storage -> storage.toPos),
				Codec.BOOL.fieldOf("isPrimary").forGetter(storage -> storage.isPrimary)
			).apply(instance, Storage::new));
		
		public Storage(Direction toSide, BlockPos toPos, boolean isPrimary)
		{
			this.toSide = toSide;
			this.toPos = toPos;
			this.isPrimary = isPrimary;
		}
		
		public static Storage fromNBT(CompoundNBT nbt)
		{
			Direction toSide = Direction.byIndex(nbt.getInt("toSide"));
			BlockPos toPos = NBTUtil.readBlockPos(nbt.getCompound("toPos"));
			boolean isPrimary = nbt.getBoolean("isPrimary");
			return new Storage(toSide, toPos, isPrimary);
		}
		
		public CompoundNBT toNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("toSide", this.toSide.ordinal());
			nbt.put("toPos", NBTUtil.writeBlockPos(this.toPos));
			nbt.putBoolean("isPrimary", this.isPrimary);
			return nbt;
		}
	}
}
