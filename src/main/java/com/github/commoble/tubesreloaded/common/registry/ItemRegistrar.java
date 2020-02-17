package com.github.commoble.tubesreloaded.common.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

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
@ObjectHolder(TubesReloadedMod.MODID)
public class ItemRegistrar
{	
	@ObjectHolder(BlockNames.OSMOSIS_SLIME)
	public static final BlockItem OSMOSIS_SLIME = null;
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		
		registerItem(registry, new BlockItem(BlockRegistrar.TUBE, new Item.Properties().group(CreativeTabs.tab)), BlockNames.TUBE);
		registerItem(registry, new BlockItem(BlockRegistrar.SHUNT, new Item.Properties().group(CreativeTabs.tab)), BlockNames.SHUNT);
		registerItem(registry, new BlockItem(BlockRegistrar.LOADER, new Item.Properties().group(CreativeTabs.tab)), BlockNames.LOADER);
		registerItem(registry, new BlockItem(BlockRegistrar.REDSTONE_TUBE, new Item.Properties().group(CreativeTabs.tab)), BlockNames.REDSTONE_TUBE);
		registerItem(registry, new BlockItem(BlockRegistrar.EXTRACTOR, new Item.Properties().group(CreativeTabs.tab)), BlockNames.EXTRACTOR);
		registerItem(registry, new BlockItem(BlockRegistrar.FILTER, new Item.Properties().group(CreativeTabs.tab)), BlockNames.FILTER);
		registerItem(registry, new BlockItem(BlockRegistrar.OSMOSIS_FILTER, new Item.Properties().group(CreativeTabs.tab)), BlockNames.OSMOSIS_FILTER);
		registerItem(registry, new BlockItem(BlockRegistrar.OSMOSIS_SLIME, new Item.Properties()), BlockNames.OSMOSIS_SLIME);
		registerItem(registry, new BlockItem(BlockRegistrar.DISTRIBUTOR, new Item.Properties().group(CreativeTabs.tab)), BlockNames.DISTRIBUTOR);
		
		IntStream.range(0, 16).forEach(
				i -> registerItem(
						registry,
						new BlockItem(
								ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TubesReloadedMod.MODID, BlockNames.COLORED_TUBE_NAMES[i])),
								new Item.Properties().group(CreativeTabs.tab)),
						BlockNames.COLORED_TUBE_NAMES[i])
				);
		// real items
		
	}
	
	private static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T newItem, String name)
	{
		String prefixedName = TubesReloadedMod.MODID + ":" + name;
		newItem.setRegistryName(prefixedName);
		registry.register(newItem);
		return newItem;
	}
}
