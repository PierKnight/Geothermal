package net.pier.geoe.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReservoirCapability implements INBTSerializable<Tag>
{
    public static final Capability<ReservoirCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
    {
        @Override
        public String toString()
        {
            return super.toString();
        }
    });

    private final Map<Long, Float> map = new HashMap<>();


    public synchronized float getValue(int chunkX, int chunkZ)
    {
        return map.getOrDefault((((long) chunkX) << 32) | (chunkZ & 0xffffffffL), -1F);
    }

    public synchronized float generateReservoir(long seed, int chunkX, int chunkZ)
    {
        long v = (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
        long pSalt = 423613082L;
        float val = new Random(seed + ((long) chunkX * chunkX * 4987142) + (chunkX * 5947611L) + (long) chunkZ * chunkZ * 4392871L + (chunkZ * 389711L) ^ pSalt).nextFloat();
        this.map.put(v, val);
        return val;
    }


    @Override
    public Tag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        CompoundTag tag = (CompoundTag) nbt;

    }
}
