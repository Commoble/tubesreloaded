package com.github.commoble.tubesreloaded.registry;

import com.github.commoble.tubesreloaded.TubesReloaded;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabs
{

	// creative tab for the stuff
	public static final ItemGroup tab = new ItemGroup(TubesReloaded.MODID) {
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(BlockRegistrar.TUBE);
		}
	};

}
