package net.pier.geoe.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.register.GeoeSounds;

public class GasLeakSoundInstance extends AbstractTickableSoundInstance {

    private final PipeBlockEntity pipeBlockEntity;
    private final Direction direction;
    protected GasLeakSoundInstance(PipeBlockEntity pipeBlockEntity, Direction direction) {
        super(GeoeSounds.GAS_LEAK.get(), SoundSource.BLOCKS);
        this.pipeBlockEntity = pipeBlockEntity;
        this.volume = 1.0f;
        this.x = pipeBlockEntity.getBlockPos().getX() + direction.getStepX() * 0.5D;
        this.y = pipeBlockEntity.getBlockPos().getY() + direction.getStepY() * 0.5D;
        this.z = pipeBlockEntity.getBlockPos().getZ() + direction.getStepZ() * 0.5D;
        this.looping = true;
        this.direction = direction;
    }

    @Override
    public void tick() {

        if(pipeBlockEntity.isRemoved()) {
            this.stop();
        }
        else if(!pipeBlockEntity.getFluidStack().getFluid().getAttributes().isGaseous()) {
            this.stop();
        }
        else if(this.pipeBlockEntity.getLevel() != null && GeothermalPipeBlock.IsBlockingLeak(this.pipeBlockEntity.getLevel(), pipeBlockEntity.getBlockPos(), this.direction))
        {
            this.stop();
        }
    }
}
