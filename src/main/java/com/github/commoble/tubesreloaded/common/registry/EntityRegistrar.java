package com.github.commoble.tubesreloaded.common.registry;

import com.github.commoble.tubesreloaded.common.itemwrapper.ItemShipmentEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;

public class EntityRegistrar
{
	public static void registerItems(RegistryEvent.Register<EntityType<?>> event)
	{
		event.getRegistry().register(new EntityType<ItemShipmentEntity>(
				ItemShipmentEntity::new,
				EntityClassification.MISC,
				true, false, true, null,
				EntitySize.fixed(1F, 1F),
				suppliesVelocityUpdates -> false,
				suppliesTrackingRange -> 16,	// seems to be range of visibility to players?
				suppliesUpdateInterval -> 10,	// default for fireball-like projectiles (not arrows)
				null));
	}
}
