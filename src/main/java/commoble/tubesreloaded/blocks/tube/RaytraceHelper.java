package commoble.tubesreloaded.blocks.tube;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.util.NestedBoundingBox;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class RaytraceHelper
{
	public static Vector3d[] getInterpolatedDifferences(Vector3d vector)
	{
		int points = 17; // 16 segments
		Vector3d[] list = new Vector3d[points];

		double dx = vector.getX();
		double dy = vector.getY();
		double dz = vector.getZ();

		for (int point = 0; point < points; point++)
		{
			double startLerp = getFractionalLerp(point, points - 1);
			list[point] = new Vector3d(startLerp * dx, startLerp * dy, startLerp * dz);
		}

		return list;
	}

	public static Vector3d[] getInterpolatedPoints(Vector3d lower, Vector3d upper)
	{
		Vector3d diff = upper.subtract(lower);
		Vector3d[] diffs = getInterpolatedDifferences(diff);
		Vector3d[] points = new Vector3d[diffs.length];
		for (int i = 0; i < points.length; i++)
		{
			points[i] = lower.add(diffs[i]);
		}
		return points;
	}

	
	/**
	 * Checks if a placed block would intersect any of the connections of the tube block at the given position
	 * @param tubePos the position of the tube
	 * @param placePos The position the block is being placed at
	 * @param raytraceWorld the world to do raytracing in -- must have the block we are trying to hit with the raytrace
	 * @param placeState The blockstate being placed
	 * @param checkedTubePositions The positions of tubes that have already been checked.
	 * Any tubes in this list that this tube is connected to is also connected to this tube, and this connection has been
	 * verified to not intersect the placed block, so we don't need to check again.
	 * @param connections the remote connections of the given tube
	 * @return A Vector3d of the intersecting hit, or null if there was no intersecting hit
	 */
	@Nullable
	public static Vector3d doesBlockStateIntersectTubeConnections(BlockPos tubePos, BlockPos placePos, IBlockReader raytraceWorld, @Nonnull BlockState placeState, Set<BlockPos> checkedTubePositions, Map<Direction, RemoteConnection> connections)
	{
		for (Map.Entry<Direction, RemoteConnection> entry : connections.entrySet())
		{
			RemoteConnection connection = entry.getValue();
			BlockPos pos = connection.toPos;
			if (!checkedTubePositions.contains(pos))
			{
				Direction fromSide = entry.getKey();
				Direction toSide = connection.toSide;
				Vector3d hit = doesBlockStateIntersectConnection(tubePos, fromSide, pos, toSide, placePos, placeState, connection.box, raytraceWorld);
				if (hit != null)
				{
					return hit;
				}
			}
		}
		return null;
	}
	
	@Nullable
	public static Vector3d doesBlockStateIntersectConnection(BlockPos startPos, Direction startSide, BlockPos endPos, Direction endSide, BlockPos placePos, @Nonnull BlockState placeState, NestedBoundingBox box, IBlockReader world)
	{
		VoxelShape shape = placeState.getCollisionShape(world, placePos);
		for (AxisAlignedBB aabb : shape.toBoundingBoxList())
		{
			if (box.intersects(aabb.offset(placePos)))
			{
				// if we confirm the AABB intersects, do a raytrace as well
				Vector3d startVec = RaytraceHelper.getTubeSideCenter(startPos, startSide);
				Vector3d endVec = RaytraceHelper.getTubeSideCenter(endPos, endSide);
				return RaytraceHelper.getTubeRaytraceHit(startVec, endVec, world);
			}
		}
		return null;
	}

	public static double getFractionalLerp(int current, int max)
	{
		return (double) current / (double) max;
	}
	
	/** Returns the vector representing the center of the side of a tube block **/
	public static Vector3d getTubeSideCenter(BlockPos pos, Direction side)
	{
		Vector3d center = TubeTileEntity.getCenter(pos);
		double offsetFromCenter = 4D/16D;
		double xOff = side.getXOffset() * offsetFromCenter;
		double yOff = side.getYOffset() * offsetFromCenter;
		double zOff = side.getZOffset() * offsetFromCenter;
		return center.add(xOff, yOff, zOff);
	}

	@Nullable
	public static Vector3d getTubeRaytraceHit(Vector3d startVec, Vector3d endVec, IBlockReader world)
	{
		Vector3d[] points = getInterpolatedPoints(startVec, endVec);
		int pointCount = points.length;
		int rayTraceCount = pointCount-1;
		for (int i=0; i<rayTraceCount; i++)
		{
			RayTraceContext context = new RayTraceContext(points[i], points[i+1], BlockMode.COLLIDER, FluidMode.NONE, null);
			BlockRayTraceResult result = rayTraceBlocks(world, context);
			if (result.getType() != RayTraceResult.Type.MISS)
			{
				return result.getHitVec();
			}
		}
		
		return null; // didn't hit
		
	}

	// vanilla raytracer requires a non-null entity when the context is constructed
	// we don't need an entity though
	public static BlockRayTraceResult rayTraceBlocks(IBlockReader world, RayTraceContext context)
	{
		return doRayTrace(context, (rayTraceContext, pos) ->
		{
			BlockState state = world.getBlockState(pos);
			Vector3d startVec = rayTraceContext.getStartVec();
			Vector3d endVec = rayTraceContext.getEndVec();
			VoxelShape shape = rayTraceContext.getBlockShape(state, world, pos);
			BlockRayTraceResult result = world.rayTraceBlocks(startVec, endVec, pos, shape, state);
			return result;
		}, (rayTraceContext) -> {
			Vector3d difference = rayTraceContext.getStartVec().subtract(rayTraceContext.getEndVec());
			return BlockRayTraceResult.createMiss(rayTraceContext.getEndVec(), Direction.getFacingFromVector(difference.x, difference.y, difference.z), new BlockPos(rayTraceContext.getEndVec()));
		});
	}

	static <T> T doRayTrace(RayTraceContext context, BiFunction<RayTraceContext, BlockPos, T> rayTracer, Function<RayTraceContext, T> missFactory)
	{
		Vector3d start = context.getStartVec();
		Vector3d end = context.getEndVec();
		if (start.equals(end))
		{
			return missFactory.apply(context);
		}
		else
		{
			double endX = MathHelper.lerp(-1.0E-7D, end.x, start.x);
			double endY = MathHelper.lerp(-1.0E-7D, end.y, start.y);
			double endZ = MathHelper.lerp(-1.0E-7D, end.z, start.z);
			double startX = MathHelper.lerp(-1.0E-7D, start.x, end.x);
			double startY = MathHelper.lerp(-1.0E-7D, start.y, end.y);
			double startZ = MathHelper.lerp(-1.0E-7D, start.z, end.z);
			int startXInt = MathHelper.floor(startX);
			int startYInt = MathHelper.floor(startY);
			int startZInt = MathHelper.floor(startZ);
			BlockPos.Mutable mutaPos = new BlockPos.Mutable(startXInt, startYInt, startZInt);
			T result = rayTracer.apply(context, mutaPos);
			if (result != null)
			{
				return result;
			}
			else
			{
				double dx = endX - startX;
				double dy = endY - startY;
				double dz = endZ - startZ;
				int xSign = MathHelper.signum(dx);
				int ySign = MathHelper.signum(dy);
				int zSign = MathHelper.signum(dz);
				double reciprocalX = xSign == 0 ? Double.MAX_VALUE : xSign / dx;
				double reciprocalY = ySign == 0 ? Double.MAX_VALUE : ySign / dy;
				double reciprocalZ = zSign == 0 ? Double.MAX_VALUE : zSign / dz;
				double calcX = reciprocalX * (xSign > 0 ? 1.0D - MathHelper.frac(startX) : MathHelper.frac(startX));
				double calcY = reciprocalY * (ySign > 0 ? 1.0D - MathHelper.frac(startY) : MathHelper.frac(startY));
				double calcZ = reciprocalZ * (zSign > 0 ? 1.0D - MathHelper.frac(startZ) : MathHelper.frac(startZ));

				while (calcX <= 1.0D || calcY <= 1.0D || calcZ <= 1.0D)
				{
					if (calcX < calcY)
					{
						if (calcX < calcZ)
						{
							startXInt += xSign;
							calcX += reciprocalX;
						}
						else
						{
							startZInt += zSign;
							calcZ += reciprocalZ;
						}
					}
					else if (calcY < calcZ)
					{
						startYInt += ySign;
						calcY += reciprocalY;
					}
					else
					{
						startZInt += zSign;
						calcZ += reciprocalZ;
					}

					T fallbackResult = rayTracer.apply(context, mutaPos.setPos(startXInt, startYInt, startZInt));
					if (fallbackResult != null)
					{
						return fallbackResult;
					}
				}

				return missFactory.apply(context);
			}
		}
	}
}
