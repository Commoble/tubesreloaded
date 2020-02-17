package com.github.commoble.tubesreloaded.common.registry;

import java.util.Arrays;

import net.minecraft.item.DyeColor;

// would put them in BlockRegistrar but the global objectholder doesn't jib with it
public class BlockNames
{
	public static final String TUBE = "tube";
	public static final String SHUNT = "shunt";
	public static final String LOADER = "loader";	
	public static final String REDSTONE_TUBE = "redstone_tube";
	public static final String EXTRACTOR = "extractor";
	public static final String FILTER = "filter";
	public static final String OSMOSIS_FILTER = "osmosis_filter";
	public static final String OSMOSIS_SLIME = "osmosis_slime";
	public static final String DISTRIBUTOR = "distributor";
	
	public static final String[] COLORED_TUBE_NAMES = Arrays.stream(DyeColor.values()).map(color -> color.toString() + "_tube").toArray(String[]::new);
}
