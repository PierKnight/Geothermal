package net.pier.geoe.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class GasLeakSoundInstance extends AbstractTickableSoundInstance {
    protected GasLeakSoundInstance(SoundEvent soundEvent, SoundSource soundSource, BlockPos pipePos, Direction direction) {
        super(soundEvent, soundSource);
        this.looping = true;

    }

    @Override
    public void tick() {
    }
}
