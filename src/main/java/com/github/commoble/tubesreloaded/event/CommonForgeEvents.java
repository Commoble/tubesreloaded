package com.github.commoble.tubesreloaded.event;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.tube.TubesInChunk;
import com.github.commoble.tubesreloaded.registry.Names;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = TubesReloaded.MODID, bus=Bus.FORGE)
public class CommonForgeEvents
{

	@SubscribeEvent
	public static void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event)
	{
		event.addCapability(new ResourceLocation(TubesReloaded.MODID, Names.TUBES_IN_CHUNK), new TubesInChunk());
	}
}
