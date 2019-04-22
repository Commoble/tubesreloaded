package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabs
{

	// creative tab for the stuff
	public static final ItemGroup tab = new ItemGroup(TubesReloadedMod.MODID) {
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(BlockRegistrar.BRASS_TUBE);
		}
	};

}
