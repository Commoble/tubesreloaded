package com.github.commoble.tubesreloaded.common.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.distributor.DistributorBlock;
import com.github.commoble.tubesreloaded.common.blocks.extractor.ExtractorBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisFilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisSlimeBlock;
import com.github.commoble.tubesreloaded.common.blocks.loader.LoaderBlock;
import com.github.commoble.tubesreloaded.common.blocks.shunt.ShuntBlock;
import com.github.commoble.tubesreloaded.common.blocks.tube.TubeBlock;
import com.github.commoble.tubesreloaded.common.blocks.tube.colored_tubes.ColoredTubeBlock;
import com.github.commoble.tubesreloaded.common.blocks.tube.redstone_tube.RedstoneTubeBlock;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class BlockRegistrar
{
	@ObjectHolder(BlockNames.TUBE)
	public static final TubeBlock TUBE = null;
	@ObjectHolder(BlockNames.SHUNT)
	public static final ShuntBlock SHUNT = null;
	@ObjectHolder(BlockNames.LOADER)
	public static final LoaderBlock LOADER = null;
	@ObjectHolder(BlockNames.REDSTONE_TUBE)
	public static final RedstoneTubeBlock REDSTONE_TUBE = null;
	@ObjectHolder(BlockNames.EXTRACTOR)
	public static final ExtractorBlock EXTRACTOR = null;
	@ObjectHolder(BlockNames.FILTER)
	public static final FilterBlock FILTER = null;
	@ObjectHolder(BlockNames.OSMOSIS_FILTER)
	public static final OsmosisFilterBlock OSMOSIS_FILTER = null;
	@ObjectHolder(BlockNames.OSMOSIS_SLIME)
	public static final OsmosisSlimeBlock OSMOSIS_SLIME = null;
	@ObjectHolder(BlockNames.DISTRIBUTOR)
	public static final OsmosisSlimeBlock DISTRIBUTOR = null;
	
	// no object holder for color tubes since there's too many of them

	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(registry, new TubeBlock(Block.Properties.create(Material.GLASS, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.4F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.TUBE);
		registerBlock(registry, new ShuntBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.SHUNT);
		registerBlock(registry, new LoaderBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.LOADER);
		registerBlock(registry, new RedstoneTubeBlock(Block.Properties.create(Material.GLASS, MaterialColor.GOLD).hardnessAndResistance(0.4F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.REDSTONE_TUBE);
		registerBlock(registry, new ExtractorBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.EXTRACTOR);
		registerBlock(registry, new FilterBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.FILTER);
		registerBlock(registry, new OsmosisFilterBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.OSMOSIS_FILTER);
		registerBlock(registry, new OsmosisSlimeBlock(Block.Properties.create(Material.CLAY)), BlockNames.OSMOSIS_SLIME);
		registerBlock(registry, new DistributorBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), BlockNames.DISTRIBUTOR);
		
		IntStream.range(0, 16).forEach(i -> registerBlock(
			registry,
			new ColoredTubeBlock(
					DyeColor.values()[i],
					Block.Properties.create(Material.GLASS)
						.hardnessAndResistance(0.4F)
						.harvestTool(ToolType.PICKAXE)
						.sound(SoundType.METAL)),
			BlockNames.COLORED_TUBE_NAMES[i]));
	}
	
	private static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T newBlock, String name)
	{
		String prefixedName = TubesReloadedMod.MODID + ":" + name;
		newBlock.setRegistryName(prefixedName);
		registry.register(newBlock);
		return newBlock;
	}
}
