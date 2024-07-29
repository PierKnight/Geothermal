package net.pier.geoe.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class GasParticleType extends ParticleType<FluidParticleOption> {
    public GasParticleType() {
        super(false, FluidParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<FluidParticleOption> codec() {
        return FluidParticleOption.CODEC;
    }
}
