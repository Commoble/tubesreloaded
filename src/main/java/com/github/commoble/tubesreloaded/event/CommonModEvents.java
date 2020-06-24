package com.github.commoble.tubesreloaded.event;

import com.github.commoble.tubesreloaded.TubesReloadedMod;
import com.github.commoble.tubesreloaded.network.IsWasSprintPacket;
import com.github.commoble.tubesreloaded.network.PacketHandler;
import com.github.commoble.tubesreloaded.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.registry.ContainerRegistrar;
import com.github.commoble.tubesreloaded.registry.ItemRegistrar;
import com.github.commoble.tubesreloaded.registry.TileEntityRegistrar;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Event handler for registering Blocks, Enchantments, Items, Potions, SoundEvents, and Biomes
 * @author Joseph
 *
 */
@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class CommonModEvents
{
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		BlockRegistrar.registerBlocks(event);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		ItemRegistrar.registerItems(event);
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		TileEntityRegistrar.registerTileEntities(event);
	}
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
	{
		ContainerRegistrar.registerContainers(event.getRegistry());
	}
	
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event)
	{		
		// register packets
		int packetID=0;
		PacketHandler.INSTANCE.registerMessage(packetID++,
				IsWasSprintPacket.class,
				IsWasSprintPacket::encode,
				IsWasSprintPacket::decode,
				IsWasSprintPacket::whenThisPacketIsReceived
				);
	}
}
