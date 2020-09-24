package com.github.commoble.tubesreloaded.registry;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.filter.FilterContainer;
import com.github.commoble.tubesreloaded.blocks.loader.LoaderContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloaded.MODID)
public class ContainerRegistrar
{
	@ObjectHolder(Names.LOADER)
	public static final ContainerType<LoaderContainer> LOADER = null;
	@ObjectHolder(Names.FILTER)
	public static final ContainerType<FilterContainer> FILTER = null;
	
	public static void registerContainers(IForgeRegistry<ContainerType<?>> registry)
	{
		RegistryHelper.register(registry, Names.LOADER, new ContainerType<>(LoaderContainer::new));
		RegistryHelper.register(registry, Names.FILTER, new ContainerType<>(FilterContainer::getClientContainer));
	}
}
