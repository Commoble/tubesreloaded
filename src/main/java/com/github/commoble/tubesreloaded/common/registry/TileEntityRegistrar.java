package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(BlockNames.BRASS_TUBE_NAME)
	public static final TileEntityType<TileEntityBrassTube> TE_TYPE_BRASS_TUBE = null;
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		TileEntityType.register(BlockRegistrar.BRASS_TUBE.getRegistryName().toString(), TileEntityType.Builder.create(TileEntityBrassTube::new));
	}
}
