package com.github.commoble.tubesreloaded;

import com.github.commoble.tubesreloaded.util.ConfigHelper;
import com.github.commoble.tubesreloaded.util.ConfigHelper.ConfigValueListener;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{	
	
	public ConfigValueListener<Integer> soft_tube_cap;
	public ConfigValueListener<Integer> hard_tube_cap;
	public ConfigValueListener<Integer> ticks_in_tube;
	public ConfigValueListener<Integer> max_items_in_tube;
	public ConfigValueListener<Integer> osmosis_filter_transfer_rate;
	public ConfigValueListener<Double> max_remote_tube_connection_range;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("general");
		this.soft_tube_cap = subscriber.subscribe(builder
				.comment("Soft cap on how many tubes can exist in a contiguous network of tubes. Items are transported slowlier in networks of greater size than this value, sigifying to the player that they have too many tubes.")
				.translation("tubesreloaded.config.soft_tube_cap")
				.defineInRange("soft_tube_cap", 400, 1, 10000));
		this.hard_tube_cap = subscriber.subscribe(builder
				.comment("Hard cap on how many tubes can exist in a contiguous network of tubes. If a player attempts to make a network of greater size from this value, not all tubes in the attempted network will become part of that network.")
				.translation("tubesreloaded.config.hard_tube_cap")
				.defineInRange("hard_tube_cap", 500, 1, 10000));
		this.ticks_in_tube = subscriber.subscribe(builder
				.comment("Base time in ticks that a moving itemstack spends in each individual tube block. Adjusted by other factors.")
				.translation("tubesreloaded.config.ticks_in_tube")
				.defineInRange("ticks_in_tube", 10, 1, 72000));
		this.max_items_in_tube = subscriber.subscribe(builder
				.comment("Max items that can fit in a single tube. A tube block will break if the number of itemstacks contained with them is greater than this value, dropping their items on the ground")
				.translation("tubesreloaded.config.max_items_in_tube")
				.defineInRange("max_items_in_tube", Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
		this.osmosis_filter_transfer_rate = subscriber.subscribe(builder
				.comment("Osmosis filter automatic item transfer rate in ticks per item. The default value of 8 is the same as vanilla hoppers.")
				.translation("tubesreloaded.config.osmosis_filter_transfer_rate")
				.defineInRange("osmosis_filter_transfer_rate", 8, 1, Integer.MAX_VALUE));
		this.max_remote_tube_connection_range = subscriber.subscribe(builder
			.comment("Maximum range at which tubes can be remotely connected to each other. This also affects how many nearby chunks are checked for longtube intersections when placing a block.")
			.translation("tubesreloaded.config.max_remote_tube_connection_range")
			.defineInRange("max_remote_tube_connection_range", 16D, 0D, Double.MAX_VALUE));
		builder.pop();
	}
	
}
