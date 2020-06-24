package com.github.commoble.tubesreloaded.common.blocks.tube.colored_tubes;

import com.github.commoble.tubesreloaded.common.blocks.tube.TubeBlock;

import net.minecraft.item.DyeColor;

public class ColoredTubeBlock extends TubeBlock
{
	private DyeColor color;

	public ColoredTubeBlock(DyeColor color, Properties properties)
	{
		super(properties);
		this.color = color;
	}
	
	public boolean isTubeCompatible(TubeBlock tube)
	{
		if (tube instanceof ColoredTubeBlock)
		{
			return ((ColoredTubeBlock)tube).color.equals(this.color);
		}
		else
		{
			return true;
		}
	}
}
