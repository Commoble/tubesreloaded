package commoble.tubesreloaded.util;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.PlayerData;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;

public class DirectionHelper
{
	@SuppressWarnings("resource")
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
		placeDir = context.hasSecondaryUseForPlayer() ? placeDir : placeDir.getOpposite();	// is player sneaking
		return placeDir;		
	}
}
