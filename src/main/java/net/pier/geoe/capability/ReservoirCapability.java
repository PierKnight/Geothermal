package net.pier.geoe.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.pier.geoe.capability.world.Reservoir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    private final Map<ChunkPos, Reservoir> map = new HashMap<>();

    public final HashSet<ChunkPos> tickingReservoirs = new HashSet<>();




    public Reservoir getReservoirWorldInfo(ChunkPos chunkPos)
    {
        if(this.level instanceof ServerLevel serverLevel) {


            WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(0L));
            worldgenrandom.setDecorationSeed(serverLevel.getSeed(), chunkPos.x, chunkPos.z);


            NormalNoise normalNoise = NormalNoise.create(worldgenrandom,new NormalNoise.NoiseParameters(-7,1.0));
            
            normalNoise.getValue(chunkPos.x,0,chunkPos.z);
            int capacity = 10000 + worldgenrandom.nextInt(10000);
            int temperature = 1 + worldgenrandom.nextInt(4);
            Reservoir.Type type = Reservoir.Type.values()[worldgenrandom.nextInt(Reservoir.Type.values().length)];
            int throughput = 100 + worldgenrandom.nextInt(1000);
            return new Reservoir(capacity,throughput,temperature,type);
        }
        return null;
    }

    public Reservoir getReservoir(ChunkPos chunkPos)
    {
        Reservoir reservoir = this.map.get(chunkPos);
        if(reservoir == null && this.level instanceof ServerLevel) {
            if(level.getChunkSource().hasChunk(chunkPos.x,chunkPos.z))
                this.tickingReservoirs.add(chunkPos);
            reservoir = getReservoirWorldInfo(chunkPos);
            this.map.put(chunkPos, reservoir);
        }
        return reservoir;
    }

    public void appendTickingReservoir(ChunkPos pos)
    {
        if(this.map.get(pos) != null)
            this.tickingReservoirs.add(pos);
    }


    public void tick()
    {
        this.tickingReservoirs.forEach(chunkPos -> this.map.get(chunkPos).update());
    }


    @Override
    public Tag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        ListTag networks = new ListTag();
        this.map.forEach((pos, reservoir) -> {
            CompoundTag reservoirTag = new CompoundTag();
            reservoirTag.putInt("chunkX",pos.x);
            reservoirTag.putInt("chunkZ",pos.z);
            networks.add(reservoir.save(reservoirTag));
        });
        tag.put("reservoirs", networks);

        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt)
    {
        CompoundTag tag = (CompoundTag) nbt;

        ListTag reservoirs = ((CompoundTag)nbt).getList("reservoirs", 10);

        for (int i = 0; i < reservoirs.size(); i++)
        {
            CompoundTag reservoirsCompound = reservoirs.getCompound(i);
            Reservoir reservoir = new Reservoir(reservoirsCompound);
            this.map.put(new ChunkPos(reservoirsCompound.getInt("chunkX"),reservoirsCompound.getInt("chunkZ")),reservoir);
        }
    }
}
