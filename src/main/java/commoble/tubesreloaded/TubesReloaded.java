package commoble.tubesreloaded;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import commoble.databuddy.config.ConfigHelper;
import commoble.tubesreloaded.blocks.distributor.DistributorBlock;
import commoble.tubesreloaded.blocks.distributor.DistributorBlockEntity;
import commoble.tubesreloaded.blocks.extractor.ExtractorBlock;
import commoble.tubesreloaded.blocks.filter.FilterBlock;
import commoble.tubesreloaded.blocks.filter.FilterBlockEntity;
import commoble.tubesreloaded.blocks.filter.FilterMenu;
import commoble.tubesreloaded.blocks.filter.MultiFilterBlock;
import commoble.tubesreloaded.blocks.filter.MultiFilterBlockEntity;
import commoble.tubesreloaded.blocks.filter.MultiFilterMenu;
import commoble.tubesreloaded.blocks.filter.OsmosisFilterBlock;
import commoble.tubesreloaded.blocks.filter.OsmosisFilterBlockEntity;
import commoble.tubesreloaded.blocks.filter.OsmosisSlimeBlock;
import commoble.tubesreloaded.blocks.loader.LoaderBlock;
import commoble.tubesreloaded.blocks.loader.LoaderMenu;
import commoble.tubesreloaded.blocks.shunt.ShuntBlock;
import commoble.tubesreloaded.blocks.shunt.ShuntBlockEntity;
import commoble.tubesreloaded.blocks.tube.ColoredTubeBlock;
import commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import commoble.tubesreloaded.blocks.tube.RedstoneTubeBlock;
import commoble.tubesreloaded.blocks.tube.RedstoneTubeBlockEntity;
import commoble.tubesreloaded.blocks.tube.SyncTubesInChunkPacket;
import commoble.tubesreloaded.blocks.tube.TubeBlock;
import commoble.tubesreloaded.blocks.tube.TubeBlockEntity;
import commoble.tubesreloaded.blocks.tube.TubeBreakPacket;
import commoble.tubesreloaded.blocks.tube.TubesInChunk;
import commoble.tubesreloaded.blocks.tube.TubingPliersItem;
import commoble.tubesreloaded.client.ClientEvents;
import commoble.tubesreloaded.client.FakeWorldForTubeRaytrace;
import commoble.useitemonblockevent.api.UseItemOnBlockEvent;
import commoble.useitemonblockevent.api.UseItemOnBlockEvent.UsePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloaded.MODID)
public class TubesReloaded
{
	public static final String MODID = "tubesreloaded";
	
	public static class Tags
	{
		public static class Blocks
		{
			public static final TagKey<Block> COLORED_TUBES = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "colored_tubes"));
			public static final TagKey<Block> TUBES = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "tubes"));
			public static final TagKey<Block> ROTATABLE_BY_PLIERS = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "rotatable_by_pliers"));
		}
		public static class Items
		{
			public static final TagKey<Item> COLORED_TUBES = TagKey.create(Registries.ITEM, new ResourceLocation(MODID, "colored_tubes"));
			public static final TagKey<Item> TUBES = TagKey.create(Registries.ITEM, new ResourceLocation(MODID, "tubes"));
		}
	}

	public static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MODID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);
	
	private static TubesReloaded INSTANCE;

	private final ServerConfig serverConfig;

	public final Map<DyeColor, RegistryObject<ColoredTubeBlock>> coloredTubeBlocks;
	public final RegistryObject<DistributorBlock> distributorBlock;
	public final RegistryObject<ExtractorBlock> extractorBlock;
	public final RegistryObject<FilterBlock> filterBlock;
	public final RegistryObject<MultiFilterBlock> multiFilterBlock;
	public final RegistryObject<LoaderBlock> loaderBlock;
	public final RegistryObject<OsmosisFilterBlock> osmosisFilterBlock;
	public final RegistryObject<OsmosisSlimeBlock> osmosisSlimeBlock;
	public final RegistryObject<RedstoneTubeBlock> redstoneTubeBlock;
	public final RegistryObject<ShuntBlock> shuntBlock;
	public final RegistryObject<TubeBlock> tubeBlock;
	
	public final RegistryObject<CreativeModeTab> tab;
	
	public final RegistryObject<TubingPliersItem> tubingPliers;

	public final RegistryObject<BlockEntityType<DistributorBlockEntity>> distributorEntity;
	public final RegistryObject<BlockEntityType<FilterBlockEntity>> filterEntity;
	public final RegistryObject<BlockEntityType<MultiFilterBlockEntity>> multiFilterEntity;
	public final RegistryObject<BlockEntityType<OsmosisFilterBlockEntity>> osmosisFilterEntity;
	public final RegistryObject<BlockEntityType<RedstoneTubeBlockEntity>> redstoneTubeEntity;
	public final RegistryObject<BlockEntityType<ShuntBlockEntity>> shuntEntity;
	public final RegistryObject<BlockEntityType<TubeBlockEntity>> tubeEntity;

	public final RegistryObject<MenuType<FilterMenu>> filterMenu;
	public final RegistryObject<MenuType<MultiFilterMenu>> multiFilterMenu;
	public final RegistryObject<MenuType<LoaderMenu>> loaderMenu;

	public TubesReloaded()
	{
		INSTANCE=this;
		
		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		final IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		this.serverConfig = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::create);
		
		// register registry objects
		final DeferredRegister<Block> blocks = makeDeferredRegister(modBus, Registries.BLOCK);
		final DeferredRegister<Item> items = makeDeferredRegister(modBus, Registries.ITEM);
		final DeferredRegister<CreativeModeTab> tabs = makeDeferredRegister(modBus, Registries.CREATIVE_MODE_TAB);
		final DeferredRegister<BlockEntityType<?>> blockEntities = makeDeferredRegister(modBus, Registries.BLOCK_ENTITY_TYPE);
		final DeferredRegister<MenuType<?>> containers = makeDeferredRegister(modBus, Registries.MENU);

		// blocks and blockitems
		List<RegistryObject<? extends TubeBlock>> tubeBlocksWithTubeBlockEntity = new ArrayList<>();
		
		Pair<RegistryObject<TubeBlock>,RegistryObject<BlockItem>> tubeBlockAndItem = registerBlockAndItem(blocks, items, Names.TUBE,
			() -> new TubeBlock(new ResourceLocation("tubesreloaded:block/tube"), BlockBehaviour.Properties.of()
				.instrument(NoteBlockInstrument.DIDGERIDOO)
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(0.4F)
				.sound(SoundType.METAL)),
			block -> new BlockItem(block, new Item.Properties()));
		this.tubeBlock = tubeBlockAndItem.getFirst();		
		this.shuntBlock = registerBlockAndStandardItem(blocks, items, Names.SHUNT,
			() -> new ShuntBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.loaderBlock = registerBlockAndStandardItem(blocks, items, Names.LOADER,
			() -> new LoaderBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.redstoneTubeBlock = registerBlockAndStandardItem(blocks, items, Names.REDSTONE_TUBE,
			() -> new RedstoneTubeBlock(new ResourceLocation("tubesreloaded:block/tube"), BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.strength(0.4F)
				.sound(SoundType.METAL)));
		this.extractorBlock = registerBlockAndStandardItem(blocks, items, Names.EXTRACTOR,
			() -> new ExtractorBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.filterBlock = registerBlockAndStandardItem(blocks, items, Names.FILTER,
			() -> new FilterBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.multiFilterBlock = registerBlockAndStandardItem(blocks, items, Names.MULTIFILTER,
			() -> new MultiFilterBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.osmosisFilterBlock = registerBlockAndStandardItem(blocks, items, Names.OSMOSIS_FILTER,
			() -> new OsmosisFilterBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.GRASS)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.osmosisSlimeBlock = blocks.register(Names.OSMOSIS_SLIME,
			() -> new OsmosisSlimeBlock(BlockBehaviour.Properties.of()));
		this.distributorBlock = registerBlockAndStandardItem(blocks, items, Names.DISTRIBUTOR,
			() -> new DistributorBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.strength(2F, 6F)
				.sound(SoundType.METAL)));
		this.coloredTubeBlocks = new EnumMap<>(DyeColor.class);
		for (DyeColor color : DyeColor.values())
		{
			String name = Names.COLORED_TUBE_NAMES[color.ordinal()];
			RegistryObject<ColoredTubeBlock> block = registerBlockAndStandardItem(blocks, items, name,
				() -> new ColoredTubeBlock(
					new ResourceLocation(TubesReloaded.MODID, "block/" + name),
					color,
					BlockBehaviour.Properties.of()
						.mapColor(color)
						.instrument(NoteBlockInstrument.DIDGERIDOO)
						.strength(0.4F)
						.sound(SoundType.METAL)));
			this.coloredTubeBlocks.put(color, block);
			tubeBlocksWithTubeBlockEntity.add(block);
		}
		tubeBlocksWithTubeBlockEntity.add(this.tubeBlock);
		
		this.tab = tabs.register(MODID, () -> CreativeModeTab.builder()
			.icon(() -> new ItemStack(this.tubeBlock.get().asItem()))
			.title(Component.translatable("itemGroup.tubesreloaded"))
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.displayItems((parameters, output) -> output.acceptAll(items.getEntries().stream().map(rob -> new ItemStack(rob.get())).toList()))
			.build());
		
		// notblock items
		this.tubingPliers = items.register(Names.TUBING_PLIERS, () -> new TubingPliersItem(new Item.Properties().durability(128)));
		
		// blockentity types
		this.tubeEntity = blockEntities.register(Names.TUBE,
			() -> BlockEntityType.Builder.of(
					TubeBlockEntity::new,
					tubeBlocksWithTubeBlockEntity.stream()
						.map(RegistryObject::get)
						.toArray(TubeBlock[]::new))
				.build(null));
		this.shuntEntity = blockEntities.register(Names.SHUNT,
			() -> BlockEntityType.Builder.of(ShuntBlockEntity::new, shuntBlock.get()).build(null));
		this.redstoneTubeEntity = blockEntities.register(Names.REDSTONE_TUBE,
			() -> BlockEntityType.Builder.of(RedstoneTubeBlockEntity::new, redstoneTubeBlock.get()).build(null));
		this.filterEntity = blockEntities.register(Names.FILTER,
			() -> BlockEntityType.Builder.of(FilterBlockEntity::new, filterBlock.get()).build(null));
		this.multiFilterEntity = blockEntities.register(Names.MULTIFILTER,
			() -> BlockEntityType.Builder.of(MultiFilterBlockEntity::new, multiFilterBlock.get()).build(null));
		this.osmosisFilterEntity = blockEntities.register(Names.OSMOSIS_FILTER,
			() -> BlockEntityType.Builder.of(OsmosisFilterBlockEntity::new, osmosisFilterBlock.get()).build(null));
		this.distributorEntity = blockEntities.register(Names.DISTRIBUTOR,
			() -> BlockEntityType.Builder.of(DistributorBlockEntity::new, distributorBlock.get()).build(null));
		
		// menu types
		this.loaderMenu = containers.register(Names.LOADER, () -> new MenuType<>(LoaderMenu::new, FeatureFlags.VANILLA_SET));
		this.filterMenu = containers.register(Names.FILTER, () -> new MenuType<>(FilterMenu::createClientMenu, FeatureFlags.VANILLA_SET));
		this.multiFilterMenu = containers.register(Names.MULTIFILTER, () -> new MenuType<>(MultiFilterMenu::clientMenu, FeatureFlags.VANILLA_SET));
		
		// subscribe events
		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onRegisterCapabilities);
		
		forgeBus.addGenericListener(LevelChunk.class, this::onAttachChunkCapabilities);
		forgeBus.addListener(this::onUseItemOnBlock);
		forgeBus.addListener(this::onChunkWatch);
		
		if (FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT)
		{
			ClientEvents.subscribeClientEvents(modBus, forgeBus);
		}
	}
	
	public static TubesReloaded get()
	{
		return INSTANCE;
	}
	
	public ServerConfig serverConfig()
	{
		return serverConfig;
	}
	
	// mod events
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{		
		// register packets
		int packetID=0;
		TubesReloaded.CHANNEL.registerMessage(packetID++,
			IsWasSprintPacket.class,
			IsWasSprintPacket::write,
			IsWasSprintPacket::read,
			IsWasSprintPacket::handle
			);
		TubesReloaded.CHANNEL.registerMessage(packetID++,
			TubeBreakPacket.class,
			TubeBreakPacket::write,
			TubeBreakPacket::read,
			TubeBreakPacket::handle
			);
		TubesReloaded.CHANNEL.registerMessage(packetID++,
			SyncTubesInChunkPacket.class,
			SyncTubesInChunkPacket::write,
			SyncTubesInChunkPacket::read,
			SyncTubesInChunkPacket::handle
			);
	}
	
	private void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(TubesInChunk.class);
	}
	
	// forge events
	
	private void onAttachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event)
	{
		TubesInChunk tubesInChunk = new TubesInChunk(event.getObject());
		event.addCapability(new ResourceLocation(TubesReloaded.MODID, Names.TUBES_IN_CHUNK), tubesInChunk);
		event.addListener(tubesInChunk::onCapabilityInvalidated);
	}
	
	// EntityPlaceEvent doesn't fire on clients
	private void onUseItemOnBlock(UseItemOnBlockEvent event)
	{
		UseOnContext useContext = event.getUseOnContext();
		ItemStack stack = useContext.getItemInHand();

		// we need to check world side because physical clients can have server worlds
		if (event.getUsePhase() == UsePhase.POST_BLOCK && stack.getItem() instanceof BlockItem blockItem)
		{
			Level level = useContext.getLevel();
			BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
			BlockPos placePos = placeContext.getClickedPos(); // getClickedPos is a misnomer, this is the position the block is placed at
			BlockState placementState = blockItem.getPlacementState(placeContext);
			
			if (placementState != null)
			{
				Set<ChunkPos> chunkPositions = TubesInChunk.getRelevantChunkPositionsNearPos(placePos);
				
				for (ChunkPos chunkPos : chunkPositions)
				{
					Set<BlockPos> checkedTubePositions = new HashSet<BlockPos>();
					for (BlockPos tubePos : TubesInChunk.getTubesInChunkIfLoaded(level, chunkPos))
					{
						if (level.getBlockEntity(tubePos) instanceof TubeBlockEntity tube)
						{
							Vec3 hit = RaytraceHelper.doesBlockStateIntersectTubeConnections(tube.getBlockPos(), placePos, new FakeWorldForTubeRaytrace(level, placePos, placementState), placementState, checkedTubePositions, tube.getRemoteConnections());
//							Vec3 hit = RaytraceHelper.doesBlockStateIntersectTubeConnections(tube.getBlockPos(), placePos, level, placementState, checkedTubePositions, tube.getRemoteConnections());
							if (hit != null)
							{
								Player player = placeContext.getPlayer();
								if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel)
								{
									serverPlayer.connection.send(new ClientboundSetEquipmentPacket(serverPlayer.getId(), ImmutableList.of(Pair.of(EquipmentSlot.MAINHAND, serverPlayer.getItemInHand(InteractionHand.MAIN_HAND)))));
									serverLevel.sendParticles(serverPlayer, DustParticleOptions.REDSTONE, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
								}
								else if (level.isClientSide)
								{
									level.addParticle(DustParticleOptions.REDSTONE, hit.x, hit.y, hit.z, 0.05D, 0.05D, 0.05D);
								}
								
								if (player != null)
								{
									player.playNotifySound(SoundEvents.WANDERING_TRADER_HURT, SoundSource.BLOCKS, 0.5F, 2F);
								}
								event.cancelWithResult(InteractionResult.SUCCESS);
								return;
							}
							else
							{
								checkedTubePositions.add(tubePos);
							}
						}
					}
				}
			}
		}
	}
	
	private void onChunkWatch(ChunkWatchEvent.Watch event)
	{
		// sync tube positions to clients when a chunk needs to be loaded on the client
		LevelChunk chunk = event.getChunk();
		ServerPlayer player = event.getPlayer();
		ChunkPos pos = chunk.getPos();
		chunk.getCapability(TubesInChunk.CAPABILITY).ifPresent(cap -> 
			TubesReloaded.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), new SyncTubesInChunkPacket(pos, cap.getPositions()))
		);
	}
	
	// registry helper methods
	
	private static <T> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, ResourceKey<Registry<T>> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> Pair<RegistryObject<BLOCK>,RegistryObject<ITEM>> registerBlockAndItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory,
		Function<? super BLOCK,ITEM> itemFactory)
	{
		RegistryObject<BLOCK> block = blocks.register(name, blockFactory);
		RegistryObject<ITEM> item = items.register(name, () -> itemFactory.apply(block.get()));
		return Pair.of(block, item);
	}
	
	private static <BLOCK extends Block> RegistryObject<BLOCK> registerBlockAndStandardItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory)
	{
		return registerBlockAndItem(blocks,items,name,blockFactory,
				block -> new BlockItem(block, new Item.Properties()))
			.getFirst();
	}
}