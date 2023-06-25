package commoble.tubesreloaded;

import java.util.Map;

import commoble.databuddy.datagen.SimpleModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

public record ItemDataHelper(Item item)
{
	public static ItemDataHelper create(Item item)
	{
		return new ItemDataHelper(item);
	}
	public static ItemDataHelper create(Item item, Map<ResourceLocation,SimpleModel> modelMap, SimpleModel model)
	{
		ItemDataHelper provider = new ItemDataHelper(item);
		modelMap.put(itemModel(provider.id()), model);
		return provider;
	}
	
	public ItemDataHelper recipe(Provider<FinishedRecipe> recipeProvider, FinishedRecipe recipe)
	{
		return this.recipe(recipeProvider, recipe.getId(), recipe);
	}
	
	public ItemDataHelper recipe(Provider<FinishedRecipe> recipeProvider, ResourceLocation recipeId, FinishedRecipe recipe)
	{
		recipeProvider.put(recipeId, recipe);
		return this;
	}
	
	public ItemDataHelper localize(LanguageProvider langProvider, String localizedName)
	{
		langProvider.addItem(this::item, localizedName);
		return this;
	}
	
	@SuppressWarnings("deprecation")
	@SafeVarargs
	public final ItemDataHelper tags(TagProvider<Item> tagProvider, TagKey<Item>... tags)
	{
		for (TagKey<Item> tagKey : tags)
		{
			tagProvider.tag(tagKey).add(BuiltInRegistries.ITEM.getResourceKey(item).get());
		}
		return this;
	}
	
	public ResourceLocation id()
	{
		return ForgeRegistries.ITEMS.getKey(this.item);
	}
	
	public static ResourceLocation itemModel(ResourceLocation id)
	{
		return new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
	}
}
