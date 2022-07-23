package commoble.tubesreloaded;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.ChunkPos;

public class MiscCodecs
{
	public static final Codec<ChunkPos> COMPRESSED_CHUNK_POS = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
}
