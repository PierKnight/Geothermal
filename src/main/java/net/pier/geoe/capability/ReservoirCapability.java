package net.pier.geoe.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

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

    private final Level level;

    public ReservoirCapability(Level level)
    {
        this.level = level;
    }

    private final Map<ChunkPos, Float> map = new HashMap<>();



    public Float getReservoirWorldInfo(ChunkPos chunkPos)
    {
        if(this.level instanceof ServerLevel serverLevel) {
            WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(0L));
            worldgenrandom.setDecorationSeed(serverLevel.getSeed(), chunkPos.x, chunkPos.z);
            return worldgenrandom.nextFloat();
        }
        return 0.0F;
    }

    public float getReservoir(ChunkPos chunkPos)
    {
        Float level = this.map.get(chunkPos);
        if(level == null) {
            level = getReservoirWorldInfo(chunkPos);
            this.map.put(chunkPos, level);
        }
        return level;
    }

    @Override
    public Tag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        this.map.forEach(((chunkPos, reservoir) -> {

        }));
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        CompoundTag tag = (CompoundTag) nbt;

    }
}
