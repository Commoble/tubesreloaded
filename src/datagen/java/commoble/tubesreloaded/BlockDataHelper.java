package commoble.tubesreloaded;

import java.util.Map;

import commoble.databuddy.datagen.BlockStateFile;
import commoble.databuddy.datagen.SimpleModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

public record BlockDataHelper(Block block)
{
	public static BlockDataHelper create(Block block, Map<ResourceLocation, BlockStateFile> blockstates, BlockStateFile blockstate)
	{
		BlockDataHelper helper = new BlockDataHelper(block);
		blockstates.put(helper.id(), blockstate);
		return helper;
	}
	
	public static BlockDataHelper create(Block block,
		Map<ResourceLocation, BlockStateFile> blockstates, BlockStateFile blockstate,
		Provider<LootTable> lootTables, LootTable lootTable)
	{
		BlockDataHelper helper = new BlockDataHelper(block);
		blockstates.put(helper.id(), blockstate);
		lootTables.put(TubesReloadedDatagen.formatId(helper.id(), "blocks/%s"), lootTable);
		return helper;
	}
	
	public BlockDataHelper localize(LanguageProvider langProvider, String localizedName)
	{
		langProvider.add(this.block, localizedName);
		return this;
	}
	
	@SuppressWarnings("deprecation")
	@SafeVarargs
	public final BlockDataHelper tags(TagProvider<Block> tagProvider, TagKey<Block>... tags)
	{
		for (TagKey<Block> tag : tags)
		{
			tagProvider.tag(tag).add(BuiltInRegistries.BLOCK.getResourceKey(block).get());
		}
		return this;
	}
	
	/**
	 * Adds a block model with the same name as this block (in the block models folder, e.g. modid:block/blockname)
	 * @param models models
	 * @param model model
	 * @return this
	 */
	public BlockDataHelper baseModel(Map<ResourceLocation, SimpleModel> models, SimpleModel model)
	{
		models.put(blockModel(this.block), model);
		return this;
	}
	
	/**
	 * Adds a model to a model map using a generic string formatted with this block's id
	 * @param models model map
	 * @param formatString e.g. "%s_model" where "%s" will be replaced with the block id's location
	 * @param model SimpleModel to add
	 * @return this
	 */
	public BlockDataHelper model(Map<ResourceLocation, SimpleModel> models, String formatString, SimpleModel model)
	{
		ResourceLocation id = this.id();
		models.put(new ResourceLocation(id.getNamespace(), String.format(formatString, id.getPath())), model);
		return this;
	}
	
	public ResourceLocation id()
	{
		return ForgeRegistries.BLOCKS.getKey(this.block);
	}
	
	public static ResourceLocation blockModel(Block block)
	{
		return blockModel(ForgeRegistries.BLOCKS.getKey(block));
	}
	
	public static ResourceLocation blockModel(ResourceLocation location)
	{
		return new ResourceLocation(location.getNamespace(), "block/" + location.getPath());
	}
}
