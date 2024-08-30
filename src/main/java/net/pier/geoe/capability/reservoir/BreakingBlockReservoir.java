package net.pier.geoe.capability.reservoir;

import net.minecraft.core.BlockPos;

public class BreakingBlockReservoir {

    public final BlockPos pos;
    public int progress;

    private final int progressSteps;

    private int internalProgress = 0;

    private final int delay;

    public BreakingBlockReservoir(BlockPos pos, int progress, int progressSteps, int delay) {
        this.pos = pos;
        this.progress = progress;
        this.progressSteps = progressSteps;
        this.delay = delay;
    }

    public int getProgressSteps() {
        return progressSteps;
    }

    protected void increaseProgress()
    {
        if(++this.internalProgress >= this.delay && (this.internalProgress - this.delay) % this.progressSteps == 0)
            this.progress++;
    }

    public int getDelay() {
        return delay;
    }
}
