package com.github.commoble.tubesreloaded.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.tube.TubingPliersItem;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering items and itemblocks, and keeping their references
 * also handle creative tabs since those are closely related to items
 */
@ObjectHolder(TubesReloaded.MODID)
public class ItemRegistrar
{	
	@ObjectHolder(Names.OSMOSIS_SLIME)
	public static final BlockItem OSMOSIS_SLIME = null;
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		
		// blockitems
		registerItem(registry, new BlockItem(BlockRegistrar.TUBE, new Item.Properties().group(CreativeTabs.tab)), Names.TUBE);
		registerItem(registry, new BlockItem(BlockRegistrar.SHUNT, new Item.Properties().group(CreativeTabs.tab)), Names.SHUNT);
		registerItem(registry, new BlockItem(BlockRegistrar.LOADER, new Item.Properties().group(CreativeTabs.tab)), Names.LOADER);
		registerItem(registry, new BlockItem(BlockRegistrar.REDSTONE_TUBE, new Item.Properties().group(CreativeTabs.tab)), Names.REDSTONE_TUBE);
		registerItem(registry, new BlockItem(BlockRegistrar.EXTRACTOR, new Item.Properties().group(CreativeTabs.tab)), Names.EXTRACTOR);
		registerItem(registry, new BlockItem(BlockRegistrar.FILTER, new Item.Properties().group(CreativeTabs.tab)), Names.FILTER);
		registerItem(registry, new BlockItem(BlockRegistrar.OSMOSIS_FILTER, new Item.Properties().group(CreativeTabs.tab)), Names.OSMOSIS_FILTER);
		registerItem(registry, new BlockItem(BlockRegistrar.OSMOSIS_SLIME, new Item.Properties()), Names.OSMOSIS_SLIME);
		registerItem(registry, new BlockItem(BlockRegistrar.DISTRIBUTOR, new Item.Properties().group(CreativeTabs.tab)), Names.DISTRIBUTOR);
		
		IntStream.range(0, 16).forEach(
				i -> registerItem(
						registry,
						new BlockItem(
								ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TubesReloaded.MODID, Names.COLORED_TUBE_NAMES[i])),
								new Item.Properties().group(CreativeTabs.tab)),
						Names.COLORED_TUBE_NAMES[i])
				);
		
		// real items
		registerItem(registry, new TubingPliersItem(new Item.Properties().group(CreativeTabs.tab)), Names.TUBING_PLIERS);
		
	}
	
	private static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T newItem, String name)
	{
		String prefixedName = TubesReloaded.MODID + ":" + name;
		newItem.setRegistryName(prefixedName);
		registry.register(newItem);
		return newItem;
	}
}
