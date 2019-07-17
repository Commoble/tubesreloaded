package com.github.commoble.tubesreloaded.common.util;

import com.github.commoble.tubesreloaded.client.ClientData;
import com.github.commoble.tubesreloaded.common.capability.issprintkeyheld.IsSprintKeyHeldProvider;

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
			isSprintKeyHeld = ClientData.INSTANCE.map(client -> client.isHoldingSprint).orElse(false);
		}
		else	// server thread
		{
			isSprintKeyHeld = context.getPlayer().getCapability(IsSprintKeyHeldProvider.IS_SPRINT_KEY_HELD_CAP)
					.map(cap -> cap.getIsSprintHeld())
					.orElse(false);
		}
				
		Direction placeDir = isSprintKeyHeld ? context.getFace().getOpposite() : context.getNearestLookingDirection();
		placeDir = context.isPlacerSneaking() ? placeDir : placeDir.getOpposite();
		return placeDir;		
	}
}
