package com.github.commoble.tubesreloaded.common.registry;

import java.util.Arrays;

import net.minecraft.item.DyeColor;

// would put them in BlockRegistrar but the global objectholder doesn't jib with it
public class BlockNames
{
	public static final String TUBE_NAME = "tube";
	public static final String SHUNT_NAME = "shunt";
	public static final String LOADER_NAME = "loader";	
	public static final String REDSTONE_TUBE_NAME = "redstone_tube";
	public static final String EXTRACTOR_NAME = "extractor";
	public static final String FILTER_NAME = "filter";
	public static final String OSMOSIS_FILTER_NAME = "osmosis_filter";
	
	public static final String[] COLORED_TUBE_NAMES = Arrays.stream(DyeColor.values()).map(color -> color.toString() + "_tube").toArray(String[]::new);
}
