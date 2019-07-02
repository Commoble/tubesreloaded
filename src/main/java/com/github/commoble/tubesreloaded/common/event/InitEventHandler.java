package com.github.commoble.tubesreloaded.common.event;

import java.util.Arrays;
import java.util.Queue;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.brasstube.ItemInTubeWrapper;

import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class InitEventHandler
{
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event)
	{
		//TubesReloadedPacketHandler.registerMessages();
		int[] bob = {1,2,4,1,3,27,2,140,5,7,0,1,4,10};
		IntArrayNBT testNBT = new IntArrayNBT(bob);
		Queue<Direction> q1 = ItemInTubeWrapper.decompressMoveList(testNBT);
		IntArrayNBT nbt2 = ItemInTubeWrapper.compressMoveList(q1);
		int[] jimmy = nbt2.getIntArray();
		System.out.println(Arrays.toString(bob));
		System.out.println(Arrays.toString(jimmy));
		System.out.println(Arrays.equals(bob, jimmy));
	}
	
	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event)
	{

	}
}
