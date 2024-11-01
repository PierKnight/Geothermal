package net.pier.geoe.capability.reservoir;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class ReservoirDigInfo implements INBTSerializable<CompoundTag>{
    private int depth = 0;
    private int pipes = 0;
    private int drill = 0;

    public ReservoirDigInfo()
    {
    }

    public int getPipes() {
        return pipes;
    }

    public int getDepth() {
        return depth;
    }

    public int getDrill() {
        return drill;
    }

    public void dig()
    {
        if(drill == depth)
            depth++;

        drill++;
    }

    public void addPipe()
    {
        pipes++;
    }

    public boolean removePipe()
    {
        if(this.pipes > 0)
        {
            this.pipes--;
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag depthTag = new CompoundTag();
        depthTag.putInt("depth", this.depth);
        depthTag.putInt("pipes", this.pipes);
        depthTag.putInt("drill", this.drill);
        return depthTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.depth = nbt.getInt("depth");
        this.pipes = nbt.getInt("pipes");
        this.drill = nbt.getInt("drill");
    }
}
