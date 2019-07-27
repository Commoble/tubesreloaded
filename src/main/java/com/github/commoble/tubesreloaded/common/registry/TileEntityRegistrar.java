package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.shunt.ShuntTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.tube.TubeTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.tube.redstone_tube.RedstoneTubeTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(BlockNames.TUBE_NAME)
	public static final TileEntityType<TubeTileEntity> TE_TYPE_TUBE = null;
	@ObjectHolder(BlockNames.SHUNT_NAME)
	public static final TileEntityType<ShuntTileEntity> TE_TYPE_SHUNT = null;
	@ObjectHolder(BlockNames.REDSTONE_TUBE_NAME)
	public static final TileEntityType<RedstoneTubeTileEntity> TE_TYPE_REDSTONE_TUBE = null;
	@ObjectHolder(BlockNames.FILTER_NAME)
	public static final TileEntityType<FilterTileEntity> TE_TYPE_FILTER = null;
	
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		event.getRegistry().register(TileEntityType.Builder.create(TubeTileEntity::new,BlockRegistrar.TUBE)
				.build(null)
				.setRegistryName(BlockNames.TUBE_NAME)
				);
		event.getRegistry().register(TileEntityType.Builder.create(ShuntTileEntity::new,BlockRegistrar.SHUNT)
				.build(null)
				.setRegistryName(BlockNames.SHUNT_NAME)
				);
		event.getRegistry().register(TileEntityType.Builder.create(RedstoneTubeTileEntity::new,BlockRegistrar.REDSTONE_TUBE)
				.build(null)
				.setRegistryName(BlockNames.REDSTONE_TUBE_NAME)
				);
		event.getRegistry().register(TileEntityType.Builder.create(FilterTileEntity::new,BlockRegistrar.FILTER)
				.build(null)
				.setRegistryName(BlockNames.FILTER_NAME)
				);
	}
}
