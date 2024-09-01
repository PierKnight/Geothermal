package net.pier.geoe.capability.reservoir;

import net.minecraft.client.multiplayer.ClientLevel;
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
    private final WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(0L));
    private NormalNoise normalNoise = null;

    public ReservoirCapability(Level level)
    {
        this.level = level;
        if(level instanceof ServerLevel serverLevel) {
            this.worldgenrandom.setSeed(serverLevel.getSeed());
            this.normalNoise = NormalNoise.create(worldgenrandom, new NormalNoise.NoiseParameters(-7, 1.0));
        }
    }

    private final Map<ChunkPos, Reservoir> map = new HashMap<>();

    public final HashSet<ChunkPos> tickingReservoirs = new HashSet<>();




    public synchronized Reservoir getReservoirWorldInfo(ChunkPos chunkPos)
    {
        if(this.level instanceof ServerLevel serverLevel) {

            worldgenrandom.setDecorationSeed(serverLevel.getSeed(), chunkPos.x, chunkPos.z);

            int capacity = 10000 + worldgenrandom.nextInt(10000);
            int temperature = (int) ((normalNoise.getValue(chunkPos.x,0,chunkPos.z) + 1.0F) * 100);
            Reservoir.Type type = Reservoir.Type.values()[worldgenrandom.nextInt(Reservoir.Type.values().length)];
            int throughput = 100 + worldgenrandom.nextInt(1000);
            return new Reservoir(chunkPos, capacity,throughput,temperature,type);
        }
        return null;
    }

    public synchronized Reservoir getReservoir(ChunkPos chunkPos)
    {
        Reservoir reservoir = this.map.get(chunkPos);
        if(reservoir == null) {
            if(!this.level.isClientSide) {
                if (level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z))
                    this.tickingReservoirs.add(chunkPos);
                reservoir = getReservoirWorldInfo(chunkPos);
            }
            else
                reservoir = new Reservoir(chunkPos, 0,0,0, Reservoir.Type.GEOTHERMAL);
            this.map.put(chunkPos, reservoir);
        }
        return reservoir;
    }

    public void appendTickingReservoir(ChunkPos pos)
    {
        if(this.isReservoirDirty(pos))
            this.tickingReservoirs.add(pos);
    }

    public boolean isReservoirDirty(ChunkPos chunkPos)
    {
        return this.map.get(chunkPos) != null;
    }

    public void tick()
    {
        this.tickingReservoirs.forEach(chunkPos -> this.map.get(chunkPos).update(level, chunkPos));
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
