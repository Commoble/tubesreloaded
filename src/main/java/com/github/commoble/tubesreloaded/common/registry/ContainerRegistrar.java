package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.loader.LoaderContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class ContainerRegistrar
{
	@ObjectHolder(BlockNames.LOADER)
	public static final ContainerType<LoaderContainer> LOADER = null;
	
	public static void registerContainers(IForgeRegistry<ContainerType<?>> registry)
	{
		RegistryHelper.register(registry, BlockNames.LOADER, new ContainerType<>(LoaderContainer::new));
	}
}
