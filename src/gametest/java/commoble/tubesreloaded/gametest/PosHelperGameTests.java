package commoble.tubesreloaded.gametest;

import java.util.List;

import com.mojang.math.OctahedralGroup;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.blocks.tube.TubeBlock;
import commoble.tubesreloaded.util.PosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TubesReloaded.MODID)
public class PosHelperGameTests
{
	@PrefixGameTestTemplate(false)
	@GameTest(template="empty")
	public static void testTransformPos(GameTestHelper helper)
	{
		BlockPos givenBlockPos = new BlockPos(1, 2, -3);
		List<BlockPos> expectedTransformPositions = List.of(
			new BlockPos(1, 2, -3),
			new BlockPos(-1, 2, 3),
			new BlockPos(3, 2, 1),
			new BlockPos(-3, 2, -1),
			new BlockPos(-1, 2, -3),
			new BlockPos(1, 2, 3),
			new BlockPos(-3, 2, 1),
			new BlockPos(3, 2, -1));
		
		for (int i=0; i<8; i++)
		{
			OctahedralGroup group = TubeBlock.TUBE_GROUPS[i];
			BlockPos expectedTransformPos = expectedTransformPositions.get(i);
			BlockPos actualTransformPos = PosHelper.transform(givenBlockPos, group);
			if (!expectedTransformPos.equals(actualTransformPos))
			{
				helper.fail(String.format("Group %s transform failed:\n\tbase pos:\t%s\n\texpected pos:\t%s\n\tactual pos:\t%s", group, givenBlockPos, expectedTransformPos, actualTransformPos)); 
			}
			BlockPos invertedTransformPos = PosHelper.transform(actualTransformPos, group.inverse());
			if (!invertedTransformPos.equals(givenBlockPos))
			{
				helper.fail(String.format("Group %s inversion failed:\tbase pos: %s\t\texpected pos: %s\t\tactual pos: %s", group, actualTransformPos, givenBlockPos, invertedTransformPos));
			}
		}
		
		helper.succeed();
		
	}
}
