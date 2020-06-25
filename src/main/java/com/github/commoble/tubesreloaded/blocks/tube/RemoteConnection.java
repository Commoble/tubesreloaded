package com.github.commoble.tubesreloaded.blocks.tube;

import com.github.commoble.tubesreloaded.util.NestedBoundingBox;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RemoteConnection
{
	public final Direction toSide;
	public final BlockPos toPos;
	public NestedBoundingBox box;
	
	public RemoteConnection(Direction fromSide, Direction toSide, BlockPos fromPos, BlockPos toPos)
	{
		this.toSide = toSide;
		this.toPos = toPos;
		this.box = getNestedBoundingBoxForConnectedPos(fromPos, toPos);
	}
	
	public Storage toStorage()
	{
		return new Storage(this.toSide, this.toPos);
	}
	
	public static RemoteConnection fromStorage(Storage storage, Direction fromSide, BlockPos fromPos)
	{
		return new RemoteConnection(fromSide, storage.toSide, fromPos, storage.toPos);
	}
	
	private static NestedBoundingBox getNestedBoundingBoxForConnectedPos(BlockPos from, BlockPos to)
	{
		Vec3d thisVec = TubeTileEntity.getConnectionVector(from);
		Vec3d otherVec = TubeTileEntity.getConnectionVector(to);
		boolean otherHigher = otherVec.y > thisVec.y;
		Vec3d higherVec = otherHigher ? otherVec : thisVec;
		Vec3d lowerVec = otherHigher ? thisVec : otherVec;
		Vec3d[] points = RaytraceHelper.getInterpolatedPoints(lowerVec, higherVec);
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
		
		public Storage(Direction toSide, BlockPos toPos)
		{
			this.toSide = toSide;
			this.toPos = toPos;
		}
		
		public static Storage fromNBT(CompoundNBT nbt)
		{
			Direction toSide = Direction.byIndex(nbt.getInt("toSide"));
			BlockPos toPos = NBTUtil.readBlockPos(nbt.getCompound("toPos"));
			return new Storage(toSide, toPos);
		}
		
		public CompoundNBT toNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("toSide", this.toSide.ordinal());
			nbt.put("toPos", NBTUtil.writeBlockPos(this.toPos));
			return nbt;
		}
	}
}
