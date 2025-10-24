package net.pier.geoe.capability.reservoir;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReservoirCapability implements INBTSerializable<Tag>
{
    public static final Capability<ReservoirCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final Level level;

    public ReservoirSampler reservoirSampler = null;

    public ReservoirCapability(Level level)
    {
        this.level = level;
        if(level instanceof ServerLevel serverLevel)
            this.reservoirSampler = new ReservoirSampler(serverLevel);
    }

    private final Map<ChunkPos, Reservoir> map = new HashMap<>();

    public final HashSet<ChunkPos> tickingReservoirs = new HashSet<>();




    public synchronized Reservoir getReservoirWorldInfo(ChunkPos chunkPos)
    {
        if(this.level instanceof ServerLevel serverLevel) {

            ReservoirSampler.Sample sample = this.reservoirSampler.getSample(serverLevel, chunkPos.x, chunkPos.z);
            int capacity = (int) Mth.clampedMap(sample.size(),-1.0F, 1.0F,500F,100000F);
            int heat = (int) Mth.clampedMap(sample.size(),-1.0F, 1.0F,0.5F,1.5F);
            return new Reservoir(chunkPos, capacity, heat, sample.type());
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
                reservoir = new Reservoir(chunkPos, 0,0, Reservoir.Type.GEOTHERMAL);
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
