package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import com.github.commoble.tubesreloaded.network.TubeBreakPacket;
import com.github.commoble.tubesreloaded.registry.BlockRegistrar;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientPacketHandlers
{
	
	public static void onWireBreakPacket(NetworkEvent.Context context, TubeBreakPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		
		if (world != null)
		{
			Vector3d[] points = RaytraceHelper.getInterpolatedPoints(packet.start, packet.end);
			ParticleManager manager = mc.particles;
			BlockState state = BlockRegistrar.TUBE.getDefaultState();
			
			for (Vector3d point : points)
			{
				BlockPos pos = new BlockPos(point);
				manager.addEffect(
					new DiggingParticle(world, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D, state)
						.setBlockPos(pos).multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F));
			}
		}
	}
}
