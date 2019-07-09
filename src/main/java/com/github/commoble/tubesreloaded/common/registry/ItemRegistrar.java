package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering items and itemblocks, and keeping their references
 * also handle creative tabs since those are closely related to items
 */
@ObjectHolder(TubesReloadedMod.MODID)
public class ItemRegistrar
{
	// itemblocks
	
	@ObjectHolder(BlockNames.TUBE_NAME)
	public static final Item tube = null;
	
	// real items
	
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		
		registerItem(registry, new BlockItem(BlockRegistrar.TUBE, new Item.Properties().group(CreativeTabs.tab)), BlockNames.TUBE_NAME);
		
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
