package commoble.tubesreloaded.bagofyurting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import commoble.bagofyurting.api.BagOfYurtingAPI;
import commoble.bagofyurting.api.BlockDataDeserializer;
import commoble.bagofyurting.api.BlockDataSerializer;
import commoble.bagofyurting.api.RotationUtil;
import commoble.tubesreloaded.blocks.tube.ItemInTubeWrapper;
import commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import commoble.tubesreloaded.blocks.tube.RemoteConnection;
import commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import commoble.tubesreloaded.registry.TileEntityRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BagOfYurtingProxy
{
	public static void addBagOfYurtingCompat()
	{
		BlockDataSerializer<TubeTileEntity> tubeSerializer = BagOfYurtingProxy::writeYurtedTubeData;
		BlockDataDeserializer<TubeTileEntity> tubeDeserializer = BagOfYurtingProxy::readYurtedTubeData; 
		BagOfYurtingAPI.registerBlockDataTransformer(TileEntityRegistrar.TUBE, tubeSerializer, tubeDeserializer);
		BagOfYurtingAPI.registerBlockDataTransformer(TileEntityRegistrar.REDSTONE_TUBE, tubeSerializer, tubeDeserializer);
	}
	
	static void writeYurtedTubeData(TubeTileEntity tube, CompoundNBT nbt, Rotation rotation, BlockPos minYurt, BlockPos maxYurt, BlockPos origin, BlockPos newOffset) 
	{
		// we need two convert these things before we write the data:
		// A) the remote connections via longtubes
			// we'd like to keep these if the remote tube is also within the yurting radius
			// remove all of the connections that are outside the radius,
			// then store the remaining connections as relative rotated offsets
		// B) the remaining routes of the travelling items stored in the tube
			// these just need to have their directions rotated
		
		tube.merge_buffer(); // make sure the buffer is empty and the real inventory is full
		
		// make a fake TE as it'll make storing the data easier
		TileEntity fakeTE = tube.getType().create();
		if (!(fakeTE instanceof TubeTileEntity))
			return;
		TubeTileEntity fakeTube = tube;
		World world = tube.getWorld();
		BlockPos pos = tube.getPos();
		fakeTube.setWorldAndPos(world, pos);
		
		
		// convert and store remote connections
		Map<Direction, RemoteConnection> connectionsToSave = new HashMap<>();
		Set<BlockPos> connectionsToBreak = new HashSet<>();
		tube.getRemoteConnections().forEach((dir,connection) ->
		{
			BlockPos remotePos = connection.toPos;
			if (isPosWithin(remotePos, minYurt, maxYurt)) // if we'll be yurting the tube connected via this connection too
			{
				BlockPos rotatedOffset = RotationUtil.transformBlockPos(rotation, remotePos, origin);
				Direction rotatedSide = rotation.rotate(dir);
				Direction rotatedEndSide = rotation.rotate(dir);
				RemoteConnection transformedConnection = new RemoteConnection(rotatedSide, rotatedEndSide, newOffset, rotatedOffset, connection.isPrimary);
				connectionsToSave.put(rotatedSide, transformedConnection);
			}
			else
			{
				// otherwise, make sure we destroy the connection cleanly
				connectionsToBreak.add(remotePos);
			}
		});
		connectionsToBreak.forEach(remotePos -> TubeTileEntity.removeConnection(world, pos, remotePos));
		fakeTube.setConnectionsRaw(connectionsToSave);
		
		// convert item data
		Queue<ItemInTubeWrapper> transformedInventory = new LinkedList<>();
		tube.inventory.forEach(wrapper -> transformedInventory.add(wrapper.rotate(rotation)));
		tube.inventory = new LinkedList<>(); // clear inventory just in case so there's not an extra copy of each item running around
		fakeTube.inventory = transformedInventory;
		
		fakeTube.write(nbt);
	}
	
	static void readYurtedTubeData(TubeTileEntity tube, CompoundNBT input, World world, BlockPos pos, BlockState state, Rotation rotation, BlockPos minYurt, BlockPos maxYurt, BlockPos origin)
	{
		tube.read(state, input);
		// the new (actual) tube's world and pos have NOT been correctly set yet, let's do that just in case
		tube.setWorldAndPos(world, pos);
		
		// untransform remote connections
		Map<Direction, RemoteConnection> transformedConnections = tube.getRemoteConnections();
		Map<Direction, RemoteConnection> detransformedConnections = new HashMap<>();
		transformedConnections.forEach((transformedDirection, transformedConnection) ->
		{
			Direction detransformedFromSide = rotation.rotate(transformedDirection);
			Direction detransformedToSide = rotation.rotate(transformedConnection.toSide);
			BlockPos detransformedDestination = RotationUtil.untransformBlockPos(rotation, transformedConnection.toPos, origin);
			// validate connection in new position -- make sure there's no collisions
			// check connection from the primary side so the raytraces are always the same
			boolean canMaintainConnection = (world.getTileEntity(detransformedDestination) instanceof TubeTileEntity) &&
				(transformedConnection.isPrimary
				? canMaintainConnection(world, detransformedFromSide, pos, detransformedToSide, detransformedDestination)
				: canMaintainConnection(world, detransformedToSide, detransformedDestination, detransformedFromSide, pos));
			if (canMaintainConnection)
			{
				RemoteConnection detransformedConnection = new RemoteConnection(detransformedFromSide, detransformedToSide, pos, detransformedDestination, transformedConnection.isPrimary);
				detransformedConnections.put(detransformedFromSide, detransformedConnection);
			}
		});
		tube.setConnectionsRaw(detransformedConnections);
		
		// untransform item data
		Queue<ItemInTubeWrapper> detransformedInventory = new LinkedList<>();
		tube.inventory.forEach(wrapper -> detransformedInventory.add(wrapper.rotate(rotation)));
		tube.inventory = detransformedInventory;
		
	}
	
	/**
	 * Returns true if a blockpos is within the box defined by the minimal and maximal corners
	 * @param pos pos to check
	 * @param min minimal corner
	 * @param max maximal corner
	 * @return true if a blockpos is within the box defined by min and max, false otherwise
	 */
	static boolean isPosWithin(BlockPos pos, BlockPos min, BlockPos max)
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return x >= min.getX() && y >= min.getY() && z >= min.getZ()
			&& x <= max.getX() && y <= max.getY() && z <= max.getZ();
	}
	
	static boolean canMaintainConnection(IBlockReader world, Direction fromSide, BlockPos fromPos, Direction toSide, BlockPos toPos)
	{
		// do a raytrace to check for interruptions
		Vector3d startVec = RaytraceHelper.getTubeSideCenter(fromPos, fromSide);
		Vector3d endVec = RaytraceHelper.getTubeSideCenter(toPos, toSide);
		Vector3d hit = RaytraceHelper.getTubeRaytraceHit(startVec, endVec, world);
		return hit == null;
	}
}
