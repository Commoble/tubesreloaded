package com.github.commoble.tubesreloaded.common.event;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.capability.issprintkeyheld.IsSprintKeyHeldProvider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=TubesReloadedMod.MODID, bus=Bus.FORGE)
public class CapabilityEventHandler
{
	public static final ResourceLocation IS_SPRINT_KEY_HELD = new ResourceLocation(TubesReloadedMod.MODID, "is_sprint_key_held");
	
	@SubscribeEvent
	public static void onAttachCapabilitiesToEntities(AttachCapabilitiesEvent<Entity> e)
	{
		Entity ent = e.getObject();
		if (ent instanceof PlayerEntity)
		{
			e.addCapability(IS_SPRINT_KEY_HELD, new IsSprintKeyHeldProvider());
		}
	}
}
