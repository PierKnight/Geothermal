package net.pier.geoe.register;

import com.mojang.serialization.Codec;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.client.particle.FluidParticleOption;
import net.pier.geoe.client.particle.GasParticle;
import net.pier.geoe.client.particle.GasParticleType;

import java.util.function.Function;
import java.util.function.Supplier;

public class GeoeParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Geothermal.MODID);

    public static final RegistryObject<ParticleType<FluidParticleOption>> GAS_TYPE = PARTICLE_TYPES.register("gas", GasParticleType::new);

}
