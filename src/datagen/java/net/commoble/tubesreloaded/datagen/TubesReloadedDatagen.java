package net.commoble.tubesreloaded.datagen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.JsonOps;

import net.commoble.databuddy.datagen.BlockStateFile;
import net.commoble.databuddy.datagen.BlockStateFile.Case;
import net.commoble.databuddy.datagen.BlockStateFile.Model;
import net.commoble.databuddy.datagen.BlockStateFile.Multipart;
import net.commoble.databuddy.datagen.BlockStateFile.PropertyValue;
import net.commoble.databuddy.datagen.BlockStateFile.Variants;
import net.commoble.databuddy.datagen.BlockStateFile.WhenApply;
import net.commoble.tubesreloaded.TubesReloaded;
import net.commoble.tubesreloaded.blocks.extractor.ExtractorBlock;
import net.commoble.tubesreloaded.blocks.tube.RedstoneTubeBlock;
import net.commoble.tubesreloaded.blocks.tube.TubeBlock;
import net.commoble.databuddy.datagen.JsonDataProvider;
import net.commoble.databuddy.datagen.SimpleModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid=TubesReloaded.MODID, bus=Bus.MOD)
public class TubesReloadedDatagen
{	
	@SubscribeEvent
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator dataGenerator = event.getGenerator();
		var holders = event.getLookupProvider();
		
		TubesReloaded mod = TubesReloaded.get();
		Map<ResourceLocation, BlockStateFile> blockStates = new HashMap<>();
		Map<ResourceLocation, SimpleModel> models = new HashMap<>();
		Map<ResourceLocation, Recipe<?>> recipes = new HashMap<>();
		Map<ResourceLocation, LootTable> lootTables = new HashMap<>();
		TagProvider<Block> blockTags = TagProvider.create(event, Registries.BLOCK, holders);
		TagProvider<Item> itemTags = TagProvider.create(event, Registries.ITEM, holders);
		
		LanguageProvider lang = new LanguageProvider(dataGenerator.getPackOutput(), TubesReloaded.MODID, "en_us")
		{
			@Override
			protected void addTranslations()
			{} // no
		};
		
		// blocks
		Function<Block, BlockStateFile> simpleBlockState = TubesReloadedDatagen::simpleBlockState;
		Function<Block, BlockStateFile> sixWayBlockState = TubesReloadedDatagen::sixWayBlockState;
		Function<Block, LootTable> simpleBlockLoot = TubesReloadedDatagen::simpleBlockLoot;
		
		BiFunction<Block, Function<Block,BlockStateFile>, BlockDataHelper> doBlockNoLoot = (block, states) ->
			BlockDataHelper.create(block, blockStates, states.apply(block));
		Function3<Block, Function<Block,BlockStateFile>, Function<Block,LootTable>, BlockDataHelper> doBlock = (block, states, loot) ->
			BlockDataHelper.create(block, blockStates, states.apply(block), lootTables, loot.apply(block));
		Function<Block, BlockDataHelper> simpleBlock = block -> doBlock.apply(block, simpleBlockState, simpleBlockLoot);
		mod.coloredTubeBlocks.forEach((dyeColor, regObj) ->
		{
			// capitalize each letter of each word in color name
			String[] colorWords = dyeColor.getName().split("_");
			for (int i=0; i < colorWords.length; i++)
			{
				String word = colorWords[i];
				colorWords[i] = Character.toUpperCase(word.charAt(0)) + word.substring(1);
			}
			String localizedName = String.join(" ", colorWords) + " Tube";
			doBlock.apply(regObj.get(), TubesReloadedDatagen::tubeBlockState, simpleBlockLoot)
				.baseModel(models, SimpleModel.createWithoutRenderType(trBlockPrefix("tube"))
					.addTexture("all", formatId(regObj.getId(), "block/%s")))
				.model(models, "block/%s_extension", SimpleModel.createWithoutRenderType(new ResourceLocation(TubesReloaded.MODID, "block/tube_extension"))
					.addTexture("all", formatId(regObj.getId(), "block/%s")))
				.tags(blockTags, TubesReloaded.Tags.Blocks.COLORED_TUBES)
				.localize(lang, localizedName);
		});
		simpleBlock.apply(mod.distributorBlock.get())
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE)
			.localize(lang, "Distributor");
		doBlock.apply(mod.extractorBlock.get(), TubesReloadedDatagen::extractorBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Extractor");
		doBlock.apply(mod.filterBlock.get(), sixWayBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Filter");
		doBlock.apply(mod.multiFilterBlock.get(), sixWayBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Multifilter");
		doBlock.apply(mod.loaderBlock.get(), sixWayBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Loader");
		doBlock.apply(mod.osmosisFilterBlock.get(), sixWayBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Osmosis Filter");
		doBlockNoLoot.apply(mod.osmosisSlimeBlock.get(), sixWayBlockState);
		doBlock.apply(mod.redstoneTubeBlock.get(), TubesReloadedDatagen::redstoneTubeBlockState, simpleBlockLoot)
			.model(models, "block/%s", SimpleModel.create(trBlockPrefix("tube"))
				.addTexture("all", formatId(mod.redstoneTubeBlock.getId(), "block/%s")))
			.model(models, "block/%s_on", SimpleModel.create(trBlockPrefix("tube"))
				.addTexture("all", formatId(mod.redstoneTubeBlock.getId(), "block/%s_on")))
			.tags(blockTags, TubesReloaded.Tags.Blocks.TUBES, BlockTags.MINEABLE_WITH_PICKAXE)
			.localize(lang, "Redstone Tube");
		doBlock.apply(mod.shuntBlock.get(), sixWayBlockState, simpleBlockLoot)
			.tags(blockTags, BlockTags.MINEABLE_WITH_PICKAXE, TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS)
			.localize(lang, "Shunt");
		doBlock.apply(mod.tubeBlock.get(), TubesReloadedDatagen::tubeBlockState, simpleBlockLoot)
			.tags(blockTags, TubesReloaded.Tags.Blocks.TUBES, BlockTags.MINEABLE_WITH_PICKAXE)
			.localize(lang, "Tube");
		
		// blockitems
		BiFunction<Item, Function<Item,SimpleModel>, ItemDataHelper> doItem = (item, modelFactory) ->
			ItemDataHelper.create(item, models, modelFactory.apply(item));
		Function<Item, SimpleModel> blockItemModel = item ->
			new SimpleModel(formatId(BuiltInRegistries.ITEM.getKey(item), "block/%s"), new HashMap<>(), Optional.empty());
		mod.coloredTubeBlocks.forEach((dyeColor, regObj) ->
		{
			Item item = regObj.get().asItem();
			doItem.apply(regObj.get().asItem(), blockItemModel)
				.recipe(recipes, formatId(regObj.getId(), "%s_from_gold"), RecipeHelpers.shaped(item, 8, CraftingBookCategory.BUILDING, List.of("iGi"), Map.of(
					'i', Ingredient.of(Tags.Items.INGOTS_GOLD),
					'G', Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/"+dyeColor.getName()))))))
				.recipe(recipes, formatId(regObj.getId(), "%s_from_copper"), RecipeHelpers.shaped(item, 2, CraftingBookCategory.BUILDING, List.of("iGi"), Map.of(
					'i', Ingredient.of(Tags.Items.INGOTS_COPPER),
					'G', Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/"+dyeColor.getName()))))))
				.tags(itemTags, TubesReloaded.Tags.Items.COLORED_TUBES);
		});
		doItem.apply(mod.distributorBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.distributorBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of("csc", "sGs", "csc"),
				Map.of(
					'c', Ingredient.of(Tags.Items.COBBLESTONE),
					's', Ingredient.of(mod.shuntBlock.get().asItem()),
					'G', Ingredient.of(Tags.Items.FENCE_GATES))));
		doItem.apply(mod.extractorBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.extractorBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of("h", "p", "s"),
				Map.of(
					'h', Ingredient.of(Items.HOPPER),
					'p', Ingredient.of(Items.PISTON),
					's', Ingredient.of(mod.shuntBlock.get().asItem()))));
		doItem.apply(mod.filterBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shapeless(mod.filterBlock.get().asItem(), 1, CraftingBookCategory.BUILDING, List.of(
				Ingredient.of(mod.shuntBlock.get().asItem()),
				Ingredient.of(Items.ITEM_FRAME))));
		doItem.apply(mod.multiFilterBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.multiFilterBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of("ifi", "fCf", "ifi"),
				Map.of(
					'i', Ingredient.of(Tags.Items.INGOTS_IRON),
					'f', Ingredient.of(mod.filterBlock.get().asItem()),
					'C', Ingredient.of(Tags.Items.CHESTS))));
		doItem.apply(mod.loaderBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.loaderBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of("sss", "P S", "sss"),
				Map.of(
					's', Ingredient.of(Tags.Items.COBBLESTONE),
					'S', Ingredient.of(mod.shuntBlock.get().asItem()),
					'P', Ingredient.of(Items.PISTON))));
		ItemDataHelper.create(mod.osmosisFilterBlock.get().asItem()) // model isn't generated
			.recipe(recipes, RecipeHelpers.shaped(mod.osmosisFilterBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of("f", "s", "h"),
				Map.of(
					'f', Ingredient.of(mod.filterBlock.get().asItem()),
					's', Ingredient.of(Tags.Items.SLIMEBALLS),
					'h', Ingredient.of(Items.HOPPER))));
		doItem.apply(mod.redstoneTubeBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.redstoneTubeBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of(" r ", "rtr", " r "),
				Map.of(
					'r', Ingredient.of(Tags.Items.DUSTS_REDSTONE),
					't', Ingredient.of(TubesReloaded.Tags.Items.TUBES))));
		doItem.apply(mod.shuntBlock.get().asItem(), blockItemModel)
			.recipe(recipes, RecipeHelpers.shaped(mod.shuntBlock.get().asItem(), 1, CraftingBookCategory.BUILDING,
				List.of(" t ", "tst", " t "),
				Map.of(
					's', Ingredient.of(Tags.Items.COBBLESTONE),
					't', Ingredient.of(TubesReloaded.Tags.Items.TUBES))))
			.tags(itemTags, TubesReloaded.Tags.Items.TUBES);
		doItem.apply(mod.tubeBlock.get().asItem(), blockItemModel)
			.recipe(recipes, formatId(mod.tubeBlock.getId(), "%s_from_gold"), RecipeHelpers.shaped(mod.tubeBlock.get().asItem(), 8, CraftingBookCategory.BUILDING, List.of("iGi"), Map.of(
				'i', Ingredient.of(Tags.Items.INGOTS_GOLD),
				'G', Ingredient.of(Tags.Items.GLASS_COLORLESS))))
			.recipe(recipes, formatId(mod.tubeBlock.getId(), "%s_from_copper"), RecipeHelpers.shaped(mod.tubeBlock.get().asItem(), 2, CraftingBookCategory.BUILDING, List.of("iGi"), Map.of(
				'i', Ingredient.of(Tags.Items.INGOTS_COPPER),
				'G', Ingredient.of(Tags.Items.GLASS_COLORLESS))))
			.tags(itemTags, TubesReloaded.Tags.Items.TUBES);
		
		// non-block items
		ItemDataHelper.create(mod.tubingPliers.get(), models, SimpleModel.create(new ResourceLocation("item/handheld"), SimpleModel.RenderTypes.CUTOUT)
				.addTexture("layer0", formatId(mod.tubingPliers.getId(), "item/%s")))
			.recipe(recipes, RecipeHelpers.shaped(mod.tubingPliers.get().asItem(), 1, CraftingBookCategory.MISC,
				List.of("  I", "It ", " I "),
				Map.of(
					'I', Ingredient.of(Tags.Items.INGOTS_IRON),
					't', Ingredient.of(TubesReloaded.Tags.Items.TUBES))))
			.localize(lang, "Tubing Pliers");
		
		// more tags
		blockTags.tag(TubesReloaded.Tags.Blocks.TUBES).addTag(TubesReloaded.Tags.Blocks.COLORED_TUBES);
		blockTags.tag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(TubesReloaded.Tags.Blocks.COLORED_TUBES);
		itemTags.tag(TubesReloaded.Tags.Items.TUBES).addTag(TubesReloaded.Tags.Items.COLORED_TUBES);
		
		// misc. lang keys
		lang.add("itemGroup.tubesreloaded", "Tubes Reloaded");
		
		// add the dataproviders
		BlockStateFile.addDataProvider(event, TubesReloaded.MODID, JsonOps.INSTANCE, blockStates);
		dataGenerator.addProvider(event.includeClient(), lang);
		SimpleModel.addDataProvider(event, TubesReloaded.MODID, JsonOps.INSTANCE, models);
		dataGenerator.addProvider(event.includeServer(), new JsonDataProvider<>(dataGenerator.getPackOutput(), dataGenerator, PackOutput.Target.DATA_PACK, "loot_tables", LootTable.CODEC, lootTables));
		dataGenerator.addProvider(event.includeServer(), new JsonDataProvider<>(dataGenerator.getPackOutput(), dataGenerator, PackOutput.Target.DATA_PACK, "recipes", Recipe.CODEC, recipes));
		dataGenerator.addProvider(event.includeServer(), blockTags);
		dataGenerator.addProvider(event.includeServer(), itemTags);
	}
	
	private static BlockStateFile simpleBlockState(Block block)
	{
		return BlockStateFile.variants(Variants.always(Model.create(BlockDataHelper.blockModel(block))));
	}
	
	private static BlockStateFile tubeBlockState(Block block)
	{
		ResourceLocation blockModel = BlockDataHelper.blockModel(block);
		ResourceLocation extension = formatId(blockModel, "%s_extension");
		return BlockStateFile.multipart(Multipart.builder()
			.addWhenApply(WhenApply.always(
				Model.create(blockModel)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.DOWN, true),
				Model.create(extension, BlockModelRotation.X90_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.UP, true),
				Model.create(extension, BlockModelRotation.X270_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.NORTH, true),
				Model.create(extension, BlockModelRotation.X0_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.EAST, true),
				Model.create(extension, BlockModelRotation.X0_Y90, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.SOUTH, true),
				Model.create(extension, BlockModelRotation.X0_Y180, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.WEST, true),
				Model.create(extension, BlockModelRotation.X0_Y270, true)))
			);
	}
	
	private static BlockStateFile redstoneTubeBlockState(Block block)
	{
		ResourceLocation blockModel = BlockDataHelper.blockModel(block);
		ResourceLocation offModel = blockModel;
		ResourceLocation onModel = formatId(blockModel, "%s_on");
		ResourceLocation extension = new ResourceLocation(TubesReloaded.MODID, "block/tube_extension");
		return BlockStateFile.multipart(Multipart.builder()
			.addWhenApply(WhenApply.when(
				Case.create(RedstoneTubeBlock.POWERED, false),
				Model.create(offModel)))
			.addWhenApply(WhenApply.when(
				Case.create(RedstoneTubeBlock.POWERED, true),
				Model.create(onModel)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.UP, true),
				Model.create(extension, BlockModelRotation.X270_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.DOWN, true),
				Model.create(extension, BlockModelRotation.X90_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.NORTH, true),
				Model.create(extension, BlockModelRotation.X0_Y0, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.EAST, true),
				Model.create(extension, BlockModelRotation.X0_Y90, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.SOUTH, true),
				Model.create(extension, BlockModelRotation.X0_Y180, true)))
			.addWhenApply(WhenApply.when(
				Case.create(TubeBlock.WEST, true),
				Model.create(extension, BlockModelRotation.X0_Y270, true)))
			);
	}
	
	private static BlockStateFile extractorBlockState(Block block)
	{
		ResourceLocation blockModel = BlockDataHelper.blockModel(block);
		ResourceLocation poweredBlockModel = formatId(blockModel, "%s_powered");
		var builder = Variants.builder();
		for (boolean powered : new boolean[]{false, true})
		{
			for (Direction direction : Direction.values())
			{
				ResourceLocation model = powered ? poweredBlockModel : blockModel;
				BlockModelRotation rotation = switch(direction)
				{
					case DOWN -> BlockModelRotation.X0_Y0;
					case UP -> BlockModelRotation.X180_Y0;
					case NORTH -> BlockModelRotation.X270_Y0;
					case SOUTH -> BlockModelRotation.X90_Y0;
					case WEST -> BlockModelRotation.X90_Y90;
					case EAST -> BlockModelRotation.X90_Y270;
				};
				builder.addVariant(
					List.of(
						PropertyValue.create(ExtractorBlock.FACING, direction),
						PropertyValue.create(ExtractorBlock.POWERED, powered)),
					Model.create(model, rotation, true));
			}
		}
		return BlockStateFile.variants(builder);
	}
	
	private static BlockStateFile sixWayBlockState(Block block)
	{
		var builder = Variants.builder();
		ResourceLocation model = BlockDataHelper.blockModel(block);
		for (Direction facing : Direction.values())
		{
			BlockModelRotation rotation = switch(facing)
			{
				case DOWN -> BlockModelRotation.X90_Y0;
				case UP -> BlockModelRotation.X270_Y0;
				case NORTH -> BlockModelRotation.X0_Y0;
				case SOUTH -> BlockModelRotation.X0_Y180;
				case WEST -> BlockModelRotation.X0_Y270;
				case EAST -> BlockModelRotation.X0_Y90;
			};
			builder.addVariant(
				PropertyValue.create(BlockStateProperties.FACING, facing),
				Model.create(model, rotation, true));
		}
		return BlockStateFile.variants(builder);
	}
	
	private static LootTable simpleBlockLoot(Block block)
	{
		return LootTable.lootTable()
			.setParamSet(LootContextParamSets.BLOCK)
			.withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(block))
				.when(ExplosionCondition.survivesExplosion()))
			.build();
	}
	
	/**
	 * @param original ResourceLocation
	 * @param formatString String for formatting, e.g. "block/%s"
	 * @return ResourceLocation with the original namespace as the namespace,
	 * and the original path inserted in the format string
	 */
	public static ResourceLocation formatId(ResourceLocation original, String formatString)
	{
		return new ResourceLocation(original.getNamespace(), String.format(formatString, original.getPath()));
	}
	
	public static ResourceLocation trBlockPrefix(String s)
	{
		return new ResourceLocation(TubesReloaded.MODID, "block/" + s);
	}
}
