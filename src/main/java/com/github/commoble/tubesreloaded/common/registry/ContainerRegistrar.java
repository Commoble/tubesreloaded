package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterContainer;
import com.github.commoble.tubesreloaded.common.blocks.loader.LoaderContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class ContainerRegistrar
{
	@ObjectHolder(BlockNames.LOADER)
	public static final ContainerType<LoaderContainer> LOADER = null;
	@ObjectHolder(BlockNames.FILTER)
	public static final ContainerType<FilterContainer> FILTER = null;
	
	public static void registerContainers(IForgeRegistry<ContainerType<?>> registry)
	{
		RegistryHelper.register(registry, BlockNames.LOADER, new ContainerType<>(LoaderContainer::new));
		RegistryHelper.register(registry, BlockNames.FILTER, new ContainerType<>(FilterContainer::new));
	}
}
