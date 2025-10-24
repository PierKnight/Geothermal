package net.pier.geoe.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GeoeChunk implements INBTSerializable<Tag> {

    public static final Capability<GeoeChunk> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});


    private final LevelChunk levelChunk;
    private final Level level;

    public GeoeChunk(LevelChunk levelChunk)
    {
        this.levelChunk = levelChunk;
        this.level = this.levelChunk.getLevel();
    }

    @Nullable
    public static GeoeChunk getGeoeChunk(Level level, BlockPos pos)
    {
        return level.getChunkAt(pos).getCapability(CAPABILITY).resolve().orElse(null);
    }

    public static void getGeoeChunk(Level level, BlockPos pos, Consumer<GeoeChunk> chunkConsumer)
    {
        level.getChunkAt(pos).getCapability(CAPABILITY).ifPresent(chunkConsumer::accept);
    }


    public void onLoad()
    {
    }

    public void onUnload()
    {
    }



    @Override
    public Tag serializeNBT() {

        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {

    }
}
