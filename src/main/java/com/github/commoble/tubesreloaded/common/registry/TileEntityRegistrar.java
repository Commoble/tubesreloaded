package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.loader.LoaderTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.shunt.ShuntTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.tube.TubeTileEntity;

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
	@ObjectHolder(BlockNames.LOADER_NAME)
	public static final TileEntityType<LoaderTileEntity> TE_TYPE_LOADER = null;
	
	
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
		event.getRegistry().register(TileEntityType.Builder.create(LoaderTileEntity::new,BlockRegistrar.LOADER)
				.build(null)
				.setRegistryName(BlockNames.LOADER_NAME)
				);
	}
}
