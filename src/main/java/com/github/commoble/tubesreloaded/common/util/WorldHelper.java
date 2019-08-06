package com.github.commoble.tubesreloaded.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.blocks.tube.TubeTileEntity;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class WorldHelper
{
	public static List<TubeTileEntity> getTubesAdjacentTo(World world, BlockPos pos)
	{
		List<TubeTileEntity> tes = new ArrayList<TubeTileEntity>(6);
		for (Direction face : Direction.values())
		{
			BlockPos checkPos = pos.offset(face);
			TileEntity te = world.getTileEntity(checkPos);
			if (te instanceof TubeTileEntity)
			{
				tes.add((TubeTileEntity)te);
			}
		}
		
		return tes;
	}
	
	public static Stream<TubeTileEntity> getBlockPositionsAsTubeTileEntities(World world, Collection<BlockPos> posCollection)
	{
		Stream<TileEntity> teStream = posCollection.stream().map(tubePos -> world.getTileEntity(tubePos));
		Stream<TileEntity> filteredStream = teStream.filter(te -> te instanceof TubeTileEntity);
		return filteredStream.map(te -> (TubeTileEntity) te);
	}
	
	public static Optional<TileEntity> getTileEntityAt(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return Optional.ofNullable(te);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> Optional<T> getTileEntityAt(Class<? extends T> clazz, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return Optional.ofNullable(te != null && te.getClass().isAssignableFrom(clazz) ? (T)te : null);
	}
	
	public static LazyOptional<IItemHandler> getTEItemHandlerAt(World world, BlockPos pos, Direction faceOfBlockPos)
	{
		TileEntity te = world.getTileEntity(pos);
		
		return te != null ? te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceOfBlockPos) : LazyOptional.empty();
	}
	
	public static LazyOptional<IItemHandler> getTEItemHandlerAtIf(World world, BlockPos pos, Direction faceOfBlockPos, Predicate<TileEntity> pred)
	{
		TileEntity te = world.getTileEntity(pos);
		
		if (te == null)
			return LazyOptional.empty();
		
		if (!pred.test(te))
			return LazyOptional.empty();
		
		return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceOfBlockPos);
	}
	
	public static void ejectItemstack(World world, BlockPos from_pos, @Nullable Direction output_dir, ItemStack stack)
	{
		// if there is room in front of the shunt, eject items there
		double x,y,z,xVel,yVel,zVel, xOff, yOff, zOff;
		BlockPos output_pos;
		if (output_dir != null)
		{
			output_pos = from_pos.offset(output_dir);
			xOff = output_dir.getXOffset();
			yOff = output_dir.getYOffset();
			zOff = output_dir.getZOffset();
		}
		else
		{
			output_pos = from_pos;
			xOff = 0D;
			yOff = 0D;
			zOff = 0D;
		}
		if (!world.getBlockState(output_pos).isSolid())
		{
			x = from_pos.getX() + 0.5D + xOff*0.75D;
			y = from_pos.getY() + 0.25D + yOff*0.75D;
			z = from_pos.getZ() + 0.5D + zOff*0.75D;
			xVel = xOff * 0.1D;
			yVel = yOff * 0.1D;
			zVel = zOff * 0.1D;
		}
		else	// otherwise just eject items inside the shunt
		{
			x = from_pos.getX() + 0.5D;
			y = from_pos.getY() + 0.5D;
			z = from_pos.getZ() + 0.5D;
			xVel = 0D;
			yVel = 0D;
			zVel = 0D;
		}
		ItemEntity itementity = new ItemEntity(world, x, y, z, stack);
        itementity.setDefaultPickupDelay();
        itementity.setMotion(xVel,yVel,zVel);
        world.addEntity(itementity);
	}
}
