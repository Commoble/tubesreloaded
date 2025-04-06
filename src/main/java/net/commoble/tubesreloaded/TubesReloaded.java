package net.commoble.tubesreloaded;

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

import net.commoble.databuddy.config.ConfigHelper;
import net.commoble.tubesreloaded.blocks.distributor.DistributorBlock;
import net.commoble.tubesreloaded.blocks.distributor.DistributorBlockEntity;
import net.commoble.tubesreloaded.blocks.extractor.ExtractorBlock;
import net.commoble.tubesreloaded.blocks.filter.FilterBlock;
import net.commoble.tubesreloaded.blocks.filter.FilterBlockEntity;
import net.commoble.tubesreloaded.blocks.filter.FilterMenu;
import net.commoble.tubesreloaded.blocks.filter.MultiFilterBlock;
import net.commoble.tubesreloaded.blocks.filter.MultiFilterBlockEntity;
import net.commoble.tubesreloaded.blocks.filter.MultiFilterMenu;
import net.commoble.tubesreloaded.blocks.filter.OsmosisFilterBlock;
import net.commoble.tubesreloaded.blocks.filter.OsmosisFilterBlockEntity;
import net.commoble.tubesreloaded.blocks.filter.OsmosisSlimeBlock;
import net.commoble.tubesreloaded.blocks.loader.LoaderBlock;
import net.commoble.tubesreloaded.blocks.loader.LoaderMenu;
import net.commoble.tubesreloaded.blocks.shunt.ShuntBlock;
import net.commoble.tubesreloaded.blocks.shunt.ShuntBlockEntity;
import net.commoble.tubesreloaded.blocks.tube.ColoredTubeBlock;
import net.commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import net.commoble.tubesreloaded.blocks.tube.RedstoneTubeBlock;
import net.commoble.tubesreloaded.blocks.tube.RedstoneTubeBlockEntity;
import net.commoble.tubesreloaded.blocks.tube.SyncTubesInChunkPacket;
import net.commoble.tubesreloaded.blocks.tube.TubeBlock;
import net.commoble.tubesreloaded.blocks.tube.TubeBlockEntity;
import net.commoble.tubesreloaded.blocks.tube.TubeBreakPacket;
import net.commoble.tubesreloaded.blocks.tube.TubesInChunk;
import net.commoble.tubesreloaded.blocks.tube.TubingPliersItem;
import net.commoble.tubesreloaded.client.ClientEvents;
import net.commoble.tubesreloaded.client.FakeWorldForTubeRaytrace;
import net.commoble.tubesreloaded.util.BlockSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
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
import net.minecraft.world.ItemInteractionResult;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent.UsePhase;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloaded.MODID)
public class TubesReloaded
{
	public static final String MODID = "tubesreloaded";
	
	public static class Tags
	{
		public static class Blocks
		{
			public static final TagKey<Block> COLORED_TUBES = TagKey.create(Registries.BLOCK, id("colored_tubes"));
			public static final TagKey<Block> TUBES = TagKey.create(Registries.BLOCK, id("tubes"));
			public static final TagKey<Block> ROTATABLE_BY_PLIERS = TagKey.create(Registries.BLOCK, id("rotatable_by_pliers"));
		}
		public static class Items
		{
			public static final TagKey<Item> COLORED_TUBES = TagKey.create(Registries.ITEM, id("colored_tubes"));
			public static final TagKey<Item> TUBES = TagKey.create(Registries.ITEM, id("tubes"));
		}
	}
	
	private static TubesReloaded INSTANCE;

	private final ServerConfig serverConfig;

	public final Map<DyeColor, DeferredHolder<Block, ColoredTubeBlock>> coloredTubeBlocks;
	public final DeferredHolder<Block, DistributorBlock> distributorBlock;
	public final DeferredHolder<Block, ExtractorBlock> extractorBlock;
	public final DeferredHolder<Block, FilterBlock> filterBlock;
	public final DeferredHolder<Block, MultiFilterBlock> multiFilterBlock;
	public final DeferredHolder<Block, LoaderBlock> loaderBlock;
	public final DeferredHolder<Block, OsmosisFilterBlock> osmosisFilterBlock;
	public final DeferredHolder<Block, OsmosisSlimeBlock> osmosisSlimeBlock;
	public final DeferredHolder<Block, RedstoneTubeBlock> redstoneTubeBlock;
	public final DeferredHolder<Block, ShuntBlock> shuntBlock;
	public final DeferredHolder<Block, TubeBlock> tubeBlock;
	
	public final DeferredHolder<CreativeModeTab, CreativeModeTab> tab;
	
	public final DeferredHolder<Item, TubingPliersItem> tubingPliers;

	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>> distributorEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterBlockEntity>> filterEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiFilterBlockEntity>> multiFilterEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<OsmosisFilterBlockEntity>> osmosisFilterEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneTubeBlockEntity>> redstoneTubeEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShuntBlockEntity>> shuntEntity;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBlockEntity>> tubeEntity;

	public final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> filterMenu;
	public final DeferredHolder<MenuType<?>, MenuType<MultiFilterMenu>> multiFilterMenu;
	public final DeferredHolder<MenuType<?>, MenuType<LoaderMenu>> loaderMenu;
	
	public final DeferredHolder<AttachmentType<?>, AttachmentType<Set<BlockPos>>> tubesInChunkAttachment;
	
	public final DeferredHolder<DataComponentType<?>, DataComponentType<BlockSide>> plieredTubeDataComponent;

	public TubesReloaded(IEventBus modBus)
	{
		INSTANCE=this;
		
		final IEventBus gameBus = NeoForge.EVENT_BUS;
		
		this.serverConfig = ConfigHelper.register(MODID, ModConfig.Type.SERVER, ServerConfig::create);
		
		// register registry objects
		final DeferredRegister<Block> blocks = makeDeferredRegister(modBus, Registries.BLOCK);
		final DeferredRegister<Item> items = makeDeferredRegister(modBus, Registries.ITEM);
		final DeferredRegister<CreativeModeTab> tabs = makeDeferredRegister(modBus, Registries.CREATIVE_MODE_TAB);
		final DeferredRegister<BlockEntityType<?>> blockEntities = makeDeferredRegister(modBus, Registries.BLOCK_ENTITY_TYPE);
		final DeferredRegister<MenuType<?>> containers = makeDeferredRegister(modBus, Registries.MENU);
		final DeferredRegister<AttachmentType<?>> attachmentTypes = makeDeferredRegister(modBus, NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
		final DeferredRegister<DataComponentType<?>> dataComponentTypes = makeDeferredRegister(modBus, Registries.DATA_COMPONENT_TYPE);
		
		// blocks and blockitems
		List<Supplier<? extends TubeBlock>> tubeBlocksWithTubeBlockEntity = new ArrayList<>();
		
		this.tubeBlock = registerBlockAndItem(blocks, items, Names.TUBE,
			() -> new TubeBlock(id("block/tube"), BlockBehaviour.Properties.of()
				.instrument(NoteBlockInstrument.DIDGERIDOO)
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(0.4F)
				.sound(SoundType.METAL)),
			block -> new BlockItem(block, new Item.Properties()))
			.getFirst();	
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
			() -> new RedstoneTubeBlock(id("block/tube"), BlockBehaviour.Properties.of()
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
			DeferredHolder<Block, ColoredTubeBlock> block = registerBlockAndStandardItem(blocks, items, name,
				() -> new ColoredTubeBlock(
					id("block/" + name),
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
						.map(Supplier::get)
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
		
		this.tubesInChunkAttachment = attachmentTypes.register(Names.TUBES_IN_CHUNK, () -> AttachmentType.<Set<BlockPos>>builder(() -> new HashSet<>())
			.serialize(TubesInChunk.TUBE_SET_CODEC)
			.build());
		
		this.plieredTubeDataComponent = dataComponentTypes.register(Names.PLIERED_TUBE, () -> DataComponentType.<BlockSide>builder()
			.networkSynchronized(BlockSide.STREAM_CODEC)
			.build());
		
		// subscribe events
		modBus.addListener(this::onRegisterPayloadHandlers);
		modBus.addListener(this::onRegisterCapabilities);
		
		gameBus.addListener(this::onUseItemOnBlock);
		gameBus.addListener(this::onChunkWatch);
		
		if (FMLEnvironment.dist.isClient())
		{
			ClientEvents.subscribeClientEvents(modBus, gameBus);
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
	
	private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
	{
		event.registrar(MODID)
			.playToServer(IsWasSprintPacket.TYPE, IsWasSprintPacket.STREAM_CODEC, IsWasSprintPacket::handle)
			.playToClient(TubeBreakPacket.TYPE, TubeBreakPacket.STREAM_CODEC, TubeBreakPacket::handle)
			.playToClient(SyncTubesInChunkPacket.TYPE, SyncTubesInChunkPacket.STREAM_CODEC, SyncTubesInChunkPacket::handle);
	}
	
	private void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.distributorEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.filterEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.multiFilterEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.osmosisFilterEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.redstoneTubeEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.shuntEntity.get(), (be,side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.tubeEntity.get(), (be,side) -> be.getItemHandler(side));
	}
	
	// EntityPlaceEvent doesn't fire on clients
	private void onUseItemOnBlock(UseItemOnBlockEvent event)
	{
		UseOnContext useContext = event.getUseOnContext();
		ItemStack stack = useContext.getItemInHand();

		// we need to check world side because physical clients can have server worlds
		if (event.getUsePhase() == UsePhase.ITEM_AFTER_BLOCK && stack.getItem() instanceof BlockItem blockItem)
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
								event.cancelWithResult(ItemInteractionResult.SUCCESS);
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
		PacketDistributor.sendToPlayer(player, new SyncTubesInChunkPacket(pos, Set.copyOf(TubesInChunk.getTubesInChunk(chunk))));
	}
	
	// registry helper methods
	
	private static <T> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, ResourceKey<Registry<T>> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> Pair<DeferredHolder<Block, BLOCK>, DeferredHolder<Item, ITEM>> registerBlockAndItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory,
		Function<? super BLOCK,ITEM> itemFactory)
	{
		DeferredHolder<Block, BLOCK> block = blocks.register(name, blockFactory);
		DeferredHolder<Item, ITEM> item = items.register(name, () -> itemFactory.apply(block.get()));
		return Pair.of(block, item);
	}
	
	private static <BLOCK extends Block> DeferredHolder<Block, BLOCK> registerBlockAndStandardItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory)
	{
		return registerBlockAndItem(blocks,items,name,blockFactory,
				block -> new BlockItem(block, new Item.Properties()))
			.getFirst();
	}
	
	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}