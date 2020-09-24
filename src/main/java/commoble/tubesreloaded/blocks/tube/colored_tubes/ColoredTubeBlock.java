package commoble.tubesreloaded.blocks.tube.colored_tubes;

import commoble.tubesreloaded.blocks.tube.TubeBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ColoredTubeBlock extends TubeBlock
{
	private DyeColor color;

	public ColoredTubeBlock(ResourceLocation textureLocation, DyeColor color, Properties properties)
	{
		super(textureLocation, properties);
		this.color = color;
	}
	
	@Override
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
