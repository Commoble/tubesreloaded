package commoble.tubesreloaded.util;

import com.mojang.serialization.Codec;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public class ExtraCodecs
{
	public static final Codec<Direction> DIRECTION = IStringSerializable.createEnumCodec(() -> Direction.values(), Direction::byName);
}
