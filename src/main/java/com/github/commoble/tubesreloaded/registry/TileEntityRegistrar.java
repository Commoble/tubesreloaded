package com.github.commoble.tubesreloaded.registry;

import java.util.stream.IntStream;

import com.github.commoble.tubesreloaded.TubesReloaded;
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

@ObjectHolder(TubesReloaded.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(Names.TUBE)
	public static final TileEntityType<TubeTileEntity> TUBE = null;
	@ObjectHolder(Names.SHUNT)
	public static final TileEntityType<ShuntTileEntity> SHUNT = null;
	@ObjectHolder(Names.REDSTONE_TUBE)
	public static final TileEntityType<RedstoneTubeTileEntity> REDSTONE_TUBE = null;
	@ObjectHolder(Names.FILTER)
	public static final TileEntityType<FilterTileEntity> FILTER = null;
	@ObjectHolder(Names.OSMOSIS_FILTER)
	public static final TileEntityType<OsmosisFilterTileEntity> OSMOSIS_FILTER = null;
	@ObjectHolder(Names.DISTRIBUTOR)
	public static final TileEntityType<DistributorTileEntity> DISTRIBUTOR = null;
	
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		// register standard tube and colored tubes with the same TE
		Block[] tubes = new Block[17];
		IntStream.range(0, 16).forEach(i -> tubes[i] = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TubesReloaded.MODID, Names.COLORED_TUBE_NAMES[i])));
		tubes[16] = BlockRegistrar.TUBE;	// need an array with all the color tubes + the original tube since they use the same TE
		event.getRegistry().register(TileEntityType.Builder.create(TubeTileEntity::new, tubes)
				.build(null)
				.setRegistryName(Names.TUBE)
				);
		event.getRegistry().register(TileEntityType.Builder.create(ShuntTileEntity::new,BlockRegistrar.SHUNT)
				.build(null)
				.setRegistryName(Names.SHUNT)
				);
		event.getRegistry().register(TileEntityType.Builder.create(RedstoneTubeTileEntity::new,BlockRegistrar.REDSTONE_TUBE)
				.build(null)
				.setRegistryName(Names.REDSTONE_TUBE)
				);
		event.getRegistry().register(TileEntityType.Builder.create(FilterTileEntity::new,BlockRegistrar.FILTER)
				.build(null)
				.setRegistryName(Names.FILTER)
				);
		event.getRegistry().register(TileEntityType.Builder.create(OsmosisFilterTileEntity::new,BlockRegistrar.OSMOSIS_FILTER)
			.build(null)
			.setRegistryName(Names.OSMOSIS_FILTER)
			);
		event.getRegistry().register(TileEntityType.Builder.create(DistributorTileEntity::new,BlockRegistrar.DISTRIBUTOR)
			.build(null)
			.setRegistryName(Names.DISTRIBUTOR)
			);
		
		
	}
}
