package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.tube.TubeBlock;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class BlockRegistrar
{
	@ObjectHolder(BlockNames.TUBE_NAME)
	public static final TubeBlock TUBE = null;

	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(registry, new TubeBlock(Block.Properties.create(Material.IRON, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.2F, 4F).sound(SoundType.METAL)), BlockNames.TUBE_NAME);
		
	}
	
	private static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T newBlock, String name)
	{
		String prefixedName = TubesReloadedMod.MODID + ":" + name;
		newBlock.setRegistryName(prefixedName);
		registry.register(newBlock);
		return newBlock;
	}
}
