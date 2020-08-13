package com.github.commoble.tubesreloaded.client;

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

		/** render **/
		@Override
		public void func_230430_a_(MatrixStack matrix, int x, int y, float partialTicks)
		{
			this.func_230446_a_(matrix); // renderBackground
			super.func_230430_a_(matrix, x, y, partialTicks);
			this.func_230459_a_(matrix, x, y); // renderHoveredTooltip
		}

//		@Override
//		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
//		{
//			String playerName = this.playerInventory.getName().getFormattedText();
//			String windowTitle = new TranslationTextComponent(this.windowTitleTranslationKey).getFormattedText();
//			this.font.drawString(windowTitle, this.xSize/2 - this.font.getStringWidth(windowTitle)/2, 6, 4210752);	// y-value and color from dispenser, etc
//			this.font.drawString(playerName, 8, this.ySize-96+2, 4210752);
//		}
		@Override
		/** drawGuiContainerBackgroundLayer **/
		protected void func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
		{
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			this.field_230706_i_.getTextureManager().bindTexture(this.texture);
			int xStart = (this.field_230708_k_ - this.xSize) / 2; // width - xSize
			int yStart = (this.field_230709_l_ - this.ySize) / 2; // height - ySize
			this.func_238474_b_(matrix, xStart,  yStart, 0, 0, this.xSize, this.ySize); // blit
		}
	}
}
