package commoble.tubesreloaded.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import commoble.tubesreloaded.blocks.tube.TubeBlock;
import commoble.tubesreloaded.util.DirectionTransformer;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class TubeQuadRenderer
{
	public static final LoadingCache<ResourceLocation, RenderMaterial> MATERIALS = CacheBuilder.newBuilder()
		.build(
			new CacheLoader<ResourceLocation, RenderMaterial>()
			{
				@Override
				public RenderMaterial load(ResourceLocation key)
				{
					return new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, key);
				}
			});
	
	public static void renderQuads(World world, float partialTicks, BlockPos startPos, BlockPos endPos, Direction startFace, Direction endFace, MatrixStack matrix, IRenderTypeBuffer buffer, TubeBlock block)
	{
		TextureAtlasSprite textureatlassprite = MATERIALS.getUnchecked(block.textureLocation).getSprite();
		
		// this Vector3d method converts (1,2,3) into (1D, 2D, 3D)
		Vector3d startVec = Vector3d.copy(startPos);
		Vector3d endVec = Vector3d.copy(endPos);
		
		Vector3d[][] vertices = DirectionTransformer.getVertexPairs(startFace, endFace);
		Vector3d offsetToEndPos = endVec.subtract(startVec);
		
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
		
		int startLight = getPackedLight(world, startPos);
		int endLight = getPackedLight(world, endPos);

		for (int side=0; side<4; side++)
		{
			matrix.push();
			
			int vertIndexA = side;
			int vertIndexB = (side+1)%4;
			Vector3d startVertexA = vertices[vertIndexA][0];
			Vector3d startVertexB = vertices[vertIndexB][0];
			Vector3d endVertexB = vertices[vertIndexB][1].add(offsetToEndPos);
			Vector3d endVertexA = vertices[vertIndexA][1].add(offsetToEndPos);

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
			
			// need to calculate normals so vertex can have sided lighting
			// to get the normal for a vertex v1 connected to v2 and v3,
			// we take the cross product (v2 - v1) x (v3 - v1)
			// for a given quad, all four vertices should have the same normal, so we only need to calculate one of them
			// and reverse it for the reverse quad
			
			Vector3d normal = startVertexB.subtract(startVertexA).crossProduct((endVertexA.subtract(startVertexA))).normalize();
			Vector3d reverseNormal = normal.mul(-1, -1, -1);

			putVertex(matrixEntry, ivertexbuilder, xA, yA, zA, minU, maxV, startLight, normal);
			putVertex(matrixEntry, ivertexbuilder, xB, yB, zB, maxU, maxV, startLight, normal);
			putVertex(matrixEntry, ivertexbuilder, xC, yC, zC, maxU, minV, endLight, normal);
			putVertex(matrixEntry, ivertexbuilder, xD, yD, zD, minU, minV, endLight, normal);
			
			// also add the vertices in reverse order so we render the insides of the tubes
			putVertex(matrixEntry, ivertexbuilder, xD, yD, zD, minU, minV, endLight, reverseNormal);
			putVertex(matrixEntry, ivertexbuilder, xC, yC, zC, maxU, minV, endLight, reverseNormal);
			putVertex(matrixEntry, ivertexbuilder, xB, yB, zB, maxU, maxV, startLight, reverseNormal);
			putVertex(matrixEntry, ivertexbuilder, xA, yA, zA, minU, maxV, startLight, reverseNormal);

			matrix.pop();
		}
		
	}

	private static void putVertex(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, float x, float y, float z, float texU, float texV, int packedLight, Vector3d normal)
	{
		bufferIn.pos(matrixEntryIn.getMatrix(), x, y, z).color(1F,1F,1F, 1F).tex(texU, texV).overlay(0, 10).lightmap(packedLight)
			.normal(matrixEntryIn.getNormal(), (float)normal.x, (float)normal.y, (float)normal.z).endVertex();
	}
	
	public static int getPackedLight(World world, BlockPos pos)
	{
		int blockLight = world.getLightFor(LightType.BLOCK, pos);
		int skyLight = world.getLightFor(LightType.SKY, pos);
		return LightTexture.packLight(blockLight, skyLight);
	}
}
