package net.pier.geoe.capability.reservoir;

import net.minecraft.core.BlockPos;

public class BreakingBlockReservoir {

    public final BlockPos pos;
    public int progress;

    private final int progressSteps;

    private int internalProgress = 0;

    public BreakingBlockReservoir(BlockPos pos, int progress, int progressSteps) {
        this.pos = pos;
        this.progress = progress;
        this.progressSteps = progressSteps;
    }

    protected void increaseProgress()
    {
        if(++this.internalProgress % this.progressSteps == 0)
            this.progress++;
    }
}
