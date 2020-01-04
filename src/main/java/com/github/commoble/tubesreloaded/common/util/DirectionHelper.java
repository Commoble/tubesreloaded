package com.github.commoble.tubesreloaded.common.util;

import com.github.commoble.tubesreloaded.common.ClientProxy;
import com.github.commoble.tubesreloaded.common.PlayerData;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;

public class DirectionHelper
{
	public static Direction getBlockFacingForPlacement(BlockItemUseContext context)
	{
		// if sprint is being held (i.e. ctrl by default), facing is based on the face of the block that was clicked on
		// otherwise, facing is based on the look vector of the player
		// holding sneak reverses the facing of the placement to the opposite face
		boolean isSprintKeyHeld;
		if (context.getWorld().isRemote)	// client thread
		{
			isSprintKeyHeld = ClientProxy.INSTANCE.map(client -> client.isHoldingSprint).orElse(false);
		}
		else	// server thread
		{
			isSprintKeyHeld = PlayerData.getSprinting(context.getPlayer().getUniqueID());
		}
				
		Direction placeDir = isSprintKeyHeld ? context.getFace().getOpposite() : context.getNearestLookingDirection();
		placeDir = context.func_225518_g_() ? placeDir : placeDir.getOpposite();	// is player sneaking
		return placeDir;		
	}
}
