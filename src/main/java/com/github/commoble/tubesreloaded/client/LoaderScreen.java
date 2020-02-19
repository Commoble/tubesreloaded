package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.loader.LoaderContainer;
import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class LoaderScreen extends ContainerScreen<LoaderContainer>
{
	// location of GUI texture
	private static final ResourceLocation TEXTURE = new ResourceLocation(TubesReloadedMod.MODID, "textures/gui/loader.png");

	public LoaderScreen(LoaderContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);
		// size of GUI in pixels
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String name = new TranslationTextComponent(BlockRegistrar.LOADER.getTranslationKey()).getFormattedText();
		String playerName = this.playerInventory.getName().getFormattedText();
		this.font.drawString(name, this.xSize/2 - this.font.getStringWidth(name)/2, 6, 4210752);	// y-value and color from dispenser, etc
		this.font.drawString(playerName, 8, this.ySize-96+2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(TEXTURE);
		int xStart = (this.width - this.xSize) / 2;
		int yStart = (this.height - this.ySize) / 2;
		this.blit(xStart,  yStart, 0, 0, this.xSize, this.ySize);
	}

}
