package commoble.tubesreloaded.event;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.blocks.tube.ITubesInChunk;
import commoble.tubesreloaded.blocks.tube.SyncTubesInChunkPacket;
import commoble.tubesreloaded.blocks.tube.TubeBreakPacket;
import commoble.tubesreloaded.blocks.tube.TubesInChunk;
import commoble.tubesreloaded.blocks.tube.TubesInChunkCapability;
import commoble.tubesreloaded.network.IsWasSprintPacket;
import commoble.tubesreloaded.network.PacketHandler;
import commoble.tubesreloaded.registry.BlockRegistrar;
import commoble.tubesreloaded.registry.ContainerRegistrar;
import commoble.tubesreloaded.registry.ItemRegistrar;
import commoble.tubesreloaded.registry.TileEntityRegistrar;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Event handler for registering Blocks, Enchantments, Items, Potions, SoundEvents, and Biomes
 */
@Mod.EventBusSubscriber(modid = TubesReloaded.MODID, bus=Bus.MOD)
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
			IsWasSprintPacket::write,
			IsWasSprintPacket::read,
			IsWasSprintPacket::handle
			);
		PacketHandler.INSTANCE.registerMessage(packetID++,
			TubeBreakPacket.class,
			TubeBreakPacket::write,
			TubeBreakPacket::read,
			TubeBreakPacket::handle
			);
		PacketHandler.INSTANCE.registerMessage(packetID++,
			SyncTubesInChunkPacket.class,
			SyncTubesInChunkPacket::write,
			SyncTubesInChunkPacket::read,
			SyncTubesInChunkPacket::handle
			);
		
		// register capabilities
		CapabilityManager.INSTANCE.register(ITubesInChunk.class, new TubesInChunkCapability.Storage(), () -> new TubesInChunk(null));
	}
}
