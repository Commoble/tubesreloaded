package com.github.commoble.tubesreloaded.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.TubesReloadedMod;
import com.github.commoble.tubesreloaded.blocks.distributor.DistributorTileEntity;
import com.github.commoble.tubesreloaded.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.blocks.filter.OsmosisFilterTileEntity;
import com.github.commoble.tubesreloaded.blocks.shunt.ShuntTileEntity;
import com.github.commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import com.github.commoble.tubesreloaded.blocks.tube.redstone_tube.RedstoneTubeTileEntity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(TubesReloadedMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(BlockNames.TUBE)
	public static final TileEntityType<TubeTileEntity> TUBE = null;
	@ObjectHolder(BlockNames.SHUNT)
	public static final TileEntityType<ShuntTileEntity> SHUNT = null;
	@ObjectHolder(BlockNames.REDSTONE_TUBE)
	public static final TileEntityType<RedstoneTubeTileEntity> REDSTONE_TUBE = null;
	@ObjectHolder(BlockNames.FILTER)
	public static final TileEntityType<FilterTileEntity> FILTER = null;
	@ObjectHolder(BlockNames.OSMOSIS_FILTER)
	public static final TileEntityType<OsmosisFilterTileEntity> OSMOSIS_FILTER = null;
	@ObjectHolder(BlockNames.DISTRIBUTOR)
	public static final TileEntityType<DistributorTileEntity> DISTRIBUTOR = null;
	
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		// register standard tube and colored tubes with the same TE
		Block[] tubes = new Block[17];
		IntStream.range(0, 16).forEach(i -> tubes[i] = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TubesReloadedMod.MODID, BlockNames.COLORED_TUBE_NAMES[i])));
		tubes[16] = BlockRegistrar.TUBE;	// need an array with all the color tubes + the original tube since they use the same TE
		event.getRegistry().register(TileEntityType.Builder.create(TubeTileEntity::new, tubes)
				.build(null)
				.setRegistryName(BlockNames.TUBE)
				);
		event.getRegistry().register(TileEntityType.Builder.create(ShuntTileEntity::new,BlockRegistrar.SHUNT)
				.build(null)
				.setRegistryName(BlockNames.SHUNT)
				);
		event.getRegistry().register(TileEntityType.Builder.create(RedstoneTubeTileEntity::new,BlockRegistrar.REDSTONE_TUBE)
				.build(null)
				.setRegistryName(BlockNames.REDSTONE_TUBE)
				);
		event.getRegistry().register(TileEntityType.Builder.create(FilterTileEntity::new,BlockRegistrar.FILTER)
				.build(null)
				.setRegistryName(BlockNames.FILTER)
				);
		event.getRegistry().register(TileEntityType.Builder.create(OsmosisFilterTileEntity::new,BlockRegistrar.OSMOSIS_FILTER)
			.build(null)
			.setRegistryName(BlockNames.OSMOSIS_FILTER)
			);
		event.getRegistry().register(TileEntityType.Builder.create(DistributorTileEntity::new,BlockRegistrar.DISTRIBUTOR)
			.build(null)
			.setRegistryName(BlockNames.DISTRIBUTOR)
			);
		
		
	}
}
