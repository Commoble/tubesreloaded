package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.brasstube.BrassTubeTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(BlockNames.BRASS_TUBE_NAME)
	public static final TileEntityType<BrassTubeTileEntity> TE_TYPE_BRASS_TUBE = null;
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		event.getRegistry().register(TileEntityType.Builder.create(BrassTubeTileEntity::new,BlockRegistrar.BRASS_TUBE)
				.build(null)
				.setRegistryName(BlockNames.BRASS_TUBE_NAME)
				);
	}
}
