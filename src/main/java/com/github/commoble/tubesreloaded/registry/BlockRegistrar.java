package com.github.commoble.tubesreloaded.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.distributor.DistributorBlock;
import com.github.commoble.tubesreloaded.blocks.extractor.ExtractorBlock;
import com.github.commoble.tubesreloaded.blocks.filter.FilterBlock;
import com.github.commoble.tubesreloaded.blocks.filter.OsmosisFilterBlock;
import com.github.commoble.tubesreloaded.blocks.filter.OsmosisSlimeBlock;
import com.github.commoble.tubesreloaded.blocks.loader.LoaderBlock;
import com.github.commoble.tubesreloaded.blocks.shunt.ShuntBlock;
import com.github.commoble.tubesreloaded.blocks.tube.TubeBlock;
import com.github.commoble.tubesreloaded.blocks.tube.colored_tubes.ColoredTubeBlock;
import com.github.commoble.tubesreloaded.blocks.tube.redstone_tube.RedstoneTubeBlock;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloaded.MODID)
public class BlockRegistrar
{
	@ObjectHolder(Names.TUBE)
	public static final TubeBlock TUBE = null;
	@ObjectHolder(Names.SHUNT)
	public static final ShuntBlock SHUNT = null;
	@ObjectHolder(Names.LOADER)
	public static final LoaderBlock LOADER = null;
	@ObjectHolder(Names.REDSTONE_TUBE)
	public static final RedstoneTubeBlock REDSTONE_TUBE = null;
	@ObjectHolder(Names.EXTRACTOR)
	public static final ExtractorBlock EXTRACTOR = null;
	@ObjectHolder(Names.FILTER)
	public static final FilterBlock FILTER = null;
	@ObjectHolder(Names.OSMOSIS_FILTER)
	public static final OsmosisFilterBlock OSMOSIS_FILTER = null;
	@ObjectHolder(Names.OSMOSIS_SLIME)
	public static final OsmosisSlimeBlock OSMOSIS_SLIME = null;
	@ObjectHolder(Names.DISTRIBUTOR)
	public static final DistributorBlock DISTRIBUTOR = null;
	
	// no object holder for color tubes since there's too many of them

	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(registry, new TubeBlock(new ResourceLocation("tubesreloaded:block/tube"), Block.Properties.create(Material.GLASS, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.4F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.TUBE);
		registerBlock(registry, new ShuntBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.SHUNT);
		registerBlock(registry, new LoaderBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.LOADER);
		registerBlock(registry, new RedstoneTubeBlock(new ResourceLocation("tubesreloaded:block/tube"), Block.Properties.create(Material.GLASS, MaterialColor.GOLD).hardnessAndResistance(0.4F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.REDSTONE_TUBE);
		registerBlock(registry, new ExtractorBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.EXTRACTOR);
		registerBlock(registry, new FilterBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.FILTER);
		registerBlock(registry, new OsmosisFilterBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.OSMOSIS_FILTER);
		registerBlock(registry, new OsmosisSlimeBlock(Block.Properties.create(Material.CLAY)), Names.OSMOSIS_SLIME);
		registerBlock(registry, new DistributorBlock(Block.Properties.create(Material.CLAY).hardnessAndResistance(2F, 6F).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL)), Names.DISTRIBUTOR);
		
		IntStream.range(0, 16).forEach(i -> registerBlock(
			registry,
			new ColoredTubeBlock(
					new ResourceLocation(TubesReloaded.MODID, "block/" + Names.COLORED_TUBE_NAMES[i]),
					DyeColor.values()[i],
					Block.Properties.create(Material.GLASS)
						.hardnessAndResistance(0.4F)
						.harvestTool(ToolType.PICKAXE)
						.sound(SoundType.METAL)),
			Names.COLORED_TUBE_NAMES[i]));
	}
	
	private static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T newBlock, String name)
	{
		String prefixedName = TubesReloaded.MODID + ":" + name;
		newBlock.setRegistryName(prefixedName);
		registry.register(newBlock);
		return newBlock;
	}
}
