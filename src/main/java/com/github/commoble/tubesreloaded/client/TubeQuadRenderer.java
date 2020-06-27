package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.util.DirectionTransformer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TubeQuadRenderer
{
	public static void renderQuads(BlockPos startPos, BlockPos endPos, Direction startFace, Direction endFace, MatrixStack matrix, IRenderTypeBuffer buffer, ResourceLocation textureLocation)
	{
		TextureAtlasSprite textureatlassprite = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, textureLocation).getSprite();
		
		Vec3d[][] vertices = DirectionTransformer.getVertexPairs(startFace, endFace);
		Vec3d offsetToEndPos = new Vec3d(endPos.subtract(startPos));

		for (int side=0; side<4; side++)
		{
			matrix.push();

			
			IVertexBuilder ivertexbuilder = buffer.getBuffer(Atlases.getCutoutBlockType());
			float totalMinU = textureatlassprite.getMinU();
			float totalMinV = textureatlassprite.getMinV();
			float totalMaxU = textureatlassprite.getMaxU();
			float totalMaxV = textureatlassprite.getMaxV();
			float texWidth = totalMaxU - totalMinU;
			float texHeight = totalMaxV - totalMinV;
			float tubeStartX = ((6F / 16F) * texWidth) + totalMinU;
			float tubeStartY = totalMinV;
			float tubeWidth = (4F / 16F) * texWidth;
			float tubeHeight = (4F / 16F) * texHeight;
			float minU = tubeStartX;
			float minV = tubeStartY;
			float maxU = tubeStartX + tubeWidth;
			float maxV = tubeStartY + tubeHeight;
			
			int vertIndexA = side;
			int vertIndexB = (side+1)%4;
			Vec3d startVertexA = vertices[vertIndexA][0];
			Vec3d startVertexB = vertices[vertIndexB][0];
			Vec3d endVertexB = vertices[vertIndexB][1].add(offsetToEndPos);
			Vec3d endVertexA = vertices[vertIndexA][1].add(offsetToEndPos);

			float xA = (float) startVertexA.x + 0.5F;
			float xB = (float) startVertexB.x + 0.5F;
			float xC = (float) endVertexB.x + 0.5F;
			float xD = (float) endVertexA.x + 0.5F;
			float yA = (float) startVertexA.y + 0.5F;
			float yB = (float) startVertexB.y + 0.5F;
			float yC = (float) endVertexB.y + 0.5F;
			float yD = (float) endVertexA.y + 0.5F;
			float zA = (float) startVertexA.z + 0.5F;
			float zB = (float) startVertexB.z + 0.5F;
			float zC = (float) endVertexB.z + 0.5F;
			float zD = (float) endVertexA.z + 0.5F;
			
			

			MatrixStack.Entry matrixEntry = matrix.getLast();

			putVertex(matrixEntry, ivertexbuilder, xA, yA, zA, minU, maxV);
			putVertex(matrixEntry, ivertexbuilder, xB, yB, zB, maxU, maxV);
			putVertex(matrixEntry, ivertexbuilder, xC, yC, zC, maxU, minV);
			putVertex(matrixEntry, ivertexbuilder, xD, yD, zD, minU, minV);
			
			// also add the vertices in reverse order so we render the insides of the tubes
			putVertex(matrixEntry, ivertexbuilder, xD, yD, zD, minU, minV);
			putVertex(matrixEntry, ivertexbuilder, xC, yC, zC, maxU, minV);
			putVertex(matrixEntry, ivertexbuilder, xB, yB, zB, maxU, maxV);
			putVertex(matrixEntry, ivertexbuilder, xA, yA, zA, minU, maxV);

			matrix.pop();
		}
		
	}

	private static void putVertex(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, float x, float y, float z, float texU, float texV)
	{
		bufferIn.pos(matrixEntryIn.getPositionMatrix(), x, y, z).color(255, 255, 255, 255).tex(texU, texV).overlay(0, 10).lightmap(240)
			.normal(matrixEntryIn.getNormalMatrix(), 0.0F, 1.0F, 0.0F).endVertex();
	}
}
