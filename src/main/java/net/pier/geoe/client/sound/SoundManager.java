package net.pier.geoe.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.pier.geoe.register.GeoeSounds;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SoundManager {


    private static final Map<Integer, SimpleSoundInstance> gasLeaksMap = new HashMap<>();
    private static SimpleSoundInstance forGasLeak(double pX, double pY, double pZ) {
        return new SimpleSoundInstance(GeoeSounds.GAS_LEAK.get().getLocation(), SoundSource.BLOCKS, 1.0F, 1.0F, true, 0, SoundInstance.Attenuation.LINEAR, pX, pY, pZ,false);
    }


    public static void playEarthquake(ChunkPos chunkPos)
    {
        EarthquakeSoundInstance earthquakeSoundInstance = new EarthquakeSoundInstance(chunkPos);
        Minecraft.getInstance().getSoundManager().play(earthquakeSoundInstance);
    }

    public static void playGasLeak(BlockPos pos, Direction direction)
    {
        stopGasLeak(pos, direction);
        Vec3i normal = direction.getNormal();
        SimpleSoundInstance gasLeak = forGasLeak(pos.getX() + normal.getX() * 0.5D,pos.getY() + normal.getY() * 0.5D,pos.getZ() + normal.getZ() * 0.5D);
        gasLeaksMap.put(getHashCode(pos, direction), gasLeak);
        Minecraft.getInstance().getSoundManager().play(gasLeak);
    }

    public static void stopGasLeak(BlockPos pos, Direction direction)
    {
        SimpleSoundInstance sound = gasLeaksMap.remove(getHashCode(pos, direction));
        if(sound != null)
            Minecraft.getInstance().getSoundManager().stop(sound);
    }

    private static int getHashCode(BlockPos pos, Direction direction)
    {
        return pos.hashCode() * 31 + direction.ordinal();
    }



}
