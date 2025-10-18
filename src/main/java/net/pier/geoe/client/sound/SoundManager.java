package net.pier.geoe.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.pier.geoe.blockentity.PipeBlockEntity;

@OnlyIn(Dist.CLIENT)
public class SoundManager {


    public static void playEarthquake(ChunkPos chunkPos)
    {
        EarthquakeSoundInstance earthquakeSoundInstance = new EarthquakeSoundInstance(chunkPos);
        Minecraft.getInstance().getSoundManager().play(earthquakeSoundInstance);
    }

    public static void playGasLeak(PipeBlockEntity pipe, Direction direction)
    {
        Minecraft.getInstance().getSoundManager().play(new GasLeakSoundInstance(pipe, direction));
    }

    private static int getHashCode(BlockPos pos, Direction direction)
    {
        return pos.hashCode() * 31 + direction.ordinal();
    }



}
