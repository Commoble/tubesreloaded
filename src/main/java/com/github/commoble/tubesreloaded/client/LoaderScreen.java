package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.loader.LoaderContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class LoaderScreen extends ContainerScreen<LoaderContainer>
{
	// location of GUI texture
	private static final ResourceLocation TEXTURE = new ResourceLocation(TubesReloaded.MODID, "textures/gui/loader.png");

	public LoaderScreen(LoaderContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);
		// size of GUI in pixels
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	/** render **/
	public void func_230430_a_(MatrixStack matrix, int x, int y, float partialTicks)
	{
		this.func_230446_a_(matrix); // renderBackground
		super.func_230430_a_(matrix, x, y, partialTicks); // render
		this.func_230459_a_(matrix, x, y); // renderHoveredTooltip
	}


//	@Override
//	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
//	{
//		String name = new TranslationTextComponent(BlockRegistrar.LOADER.getTranslationKey()).getFormattedText();
//		String playerName = this.playerInventory.getName().getFormattedText();
//		this.font.drawString(name, this.xSize/2 - this.font.getStringWidth(name)/2, 6, 4210752);	// y-value and color from dispenser, etc
//		this.font.drawString(playerName, 8, this.ySize-96+2, 4210752);
//	}

	@Override
	/** drawGuiContainerBackgroundLayer **/
	protected void func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.field_230706_i_.getTextureManager().bindTexture(TEXTURE);
		int xStart = (this.field_230708_k_ - this.xSize) / 2; // width - xSize
		int yStart = (this.field_230709_l_ - this.ySize) / 2; // height - ySize
		this.func_238474_b_(matrix, xStart,  yStart, 0, 0, this.xSize, this.ySize); // blit
	}

}
