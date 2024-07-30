package net.pier.geoe.register;

import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.client.particle.FluidParticleOption;
import net.pier.geoe.client.particle.GasParticleType;

public class GeoeParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Geothermal.MODID);

    public static final RegistryObject<ParticleType<FluidParticleOption>> GAS_TYPE = PARTICLE_TYPES.register("gas", GasParticleType::new);

}
