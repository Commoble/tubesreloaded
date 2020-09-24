package commoble.tubesreloaded.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class StandardSizeContainerScreenFactory<ContainerType extends Container> implements ScreenManager.IScreenFactory<ContainerType, ContainerScreen<ContainerType>>
{
	// location of GUI texture
	private final ResourceLocation texture;
	private final String windowTitleTranslationKey;

	private StandardSizeContainerScreenFactory(ResourceLocation texture, String windowTitleTranslationKey)
	{
		this.texture = texture;
		this.windowTitleTranslationKey = windowTitleTranslationKey;
	}
	
	public static <ContainerType extends Container> StandardSizeContainerScreenFactory<ContainerType> of(ResourceLocation texture, String translationKey)
	{
		return new StandardSizeContainerScreenFactory<>(texture, translationKey);
	}

	@Override
	public ContainerScreen<ContainerType> create(ContainerType container, PlayerInventory inventory, ITextComponent name)
	{
		return new StandardSizeContainerScreen<ContainerType>(container, inventory, name, this.texture, this.windowTitleTranslationKey);
	}
	
	static class StandardSizeContainerScreen<ContainerType extends Container> extends ContainerScreen<ContainerType>
	{
		private final ResourceLocation texture;
		
		public StandardSizeContainerScreen(ContainerType screenContainer, PlayerInventory inv, ITextComponent titleIn, ResourceLocation texture, String windowTitleTranslationKey)
		{
			super(screenContainer, inv, titleIn);
			this.xSize = 176;
			this.ySize = 166;
			this.texture = texture;
		}

		@Override
		public void render(MatrixStack matrix, int x, int y, float partialTicks)
		{
			this.renderBackground(matrix);
			super.render(matrix, x, y, partialTicks);
			this.renderHoveredTooltip(matrix, x, y);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
		{
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			this.minecraft.getTextureManager().bindTexture(this.texture);
			int xStart = (this.width - this.xSize) / 2;
			int yStart = (this.height - this.ySize) / 2;
			this.blit(matrix, xStart,  yStart, 0, 0, this.xSize, this.ySize);
		}
	}
}
