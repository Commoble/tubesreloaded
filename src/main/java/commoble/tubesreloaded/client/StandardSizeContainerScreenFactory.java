package commoble.tubesreloaded.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class StandardSizeContainerScreenFactory<ContainerType extends AbstractContainerMenu> implements MenuScreens.ScreenConstructor<ContainerType, AbstractContainerScreen<ContainerType>>
{
	// location of GUI texture
	private final ResourceLocation texture;
	private final String windowTitleTranslationKey;

	private StandardSizeContainerScreenFactory(ResourceLocation texture, String windowTitleTranslationKey)
	{
		this.texture = texture;
		this.windowTitleTranslationKey = windowTitleTranslationKey;
	}
	
	public static <ContainerType extends AbstractContainerMenu> StandardSizeContainerScreenFactory<ContainerType> of(ResourceLocation texture, String translationKey)
	{
		return new StandardSizeContainerScreenFactory<>(texture, translationKey);
	}

	@Override
	public AbstractContainerScreen<ContainerType> create(ContainerType container, Inventory inventory, Component name)
	{
		return new StandardSizeContainerScreen<ContainerType>(container, inventory, name, this.texture, this.windowTitleTranslationKey);
	}
	
	static class StandardSizeContainerScreen<ContainerType extends AbstractContainerMenu> extends AbstractContainerScreen<ContainerType>
	{
		private final ResourceLocation texture;
		
		public StandardSizeContainerScreen(ContainerType screenContainer, Inventory inv, Component titleIn, ResourceLocation texture, String windowTitleTranslationKey)
		{
			super(screenContainer, inv, titleIn);
			this.imageWidth = 176;
			this.imageHeight = 166;
			this.texture = texture;
		}

		@Override
		public void render(PoseStack matrix, int x, int y, float partialTicks)
		{
			this.renderBackground(matrix);
			super.render(matrix, x, y, partialTicks);
			this.renderTooltip(matrix, x, y);
		}
		
		@Override
		protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
		{
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, this.texture);
			int xStart = (this.width - this.imageWidth) / 2;
			int yStart = (this.height - this.imageHeight) / 2;
			this.blit(matrix, xStart,  yStart, 0, 0, this.imageWidth, this.imageHeight);
		}
	}
}
