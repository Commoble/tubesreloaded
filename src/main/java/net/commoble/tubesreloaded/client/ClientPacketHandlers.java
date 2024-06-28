package net.commoble.tubesreloaded.client;

import net.commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import net.commoble.tubesreloaded.blocks.tube.TubeBreakPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClientPacketHandlers
{
	public static void onTubeBreakPacket(TubeBreakPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		
		if (level != null)
		{
			Vec3[] points = RaytraceHelper.getInterpolatedPoints(packet.start(), packet.end());
			ParticleEngine manager = mc.particleEngine;
			BlockState state = level.getBlockState(BlockPos.containing(packet.start()));
			
			for (Vec3 point : points)
			{
				manager.add(
					new TerrainParticle(level, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D, state)
						.setPower(0.2F).scale(0.6F));
			}
		}
	}
}
