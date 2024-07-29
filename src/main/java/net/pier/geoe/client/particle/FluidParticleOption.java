package net.pier.geoe.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.pier.geoe.register.GeoeParticleTypes;

import static net.minecraft.commands.arguments.blocks.BlockStateParser.ERROR_UNKNOWN_BLOCK;

public record FluidParticleOption(Fluid fluid) implements ParticleOptions  {

    public static final ParticleOptions.Deserializer<FluidParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<FluidParticleOption>() {
        public FluidParticleOption fromCommand(ParticleType<FluidParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            return new FluidParticleOption(this.getFluid(stringReader));
        }

        public FluidParticleOption fromNetwork(ParticleType<FluidParticleOption> particleType, FriendlyByteBuf buf) {
            return new FluidParticleOption(buf.readRegistryId());
        }

        private Fluid getFluid(StringReader stringReader) throws CommandSyntaxException
        {
            int cursor = stringReader.getCursor();
            ResourceLocation id = ResourceLocation.read(stringReader);
            return ForgeRegistries.FLUIDS.getHolder(id).orElseThrow(() -> {
                stringReader.setCursor(cursor);
                return ERROR_UNKNOWN_BLOCK.createWithContext(stringReader, id.toString());
            }).value();
        }
    };

    public static final Codec<FluidParticleOption> CODEC =  Registry.FLUID.byNameCodec().xmap(FluidParticleOption::new, (particleOption) -> particleOption.fluid);

    @Override
    public ParticleType<?> getType() {
        return GeoeParticleTypes.GAS_TYPE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeRegistryId(this.fluid);
    }

    @Override
    public String writeToString() {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + this.fluid.getRegistryName();
    }

    public Fluid getFluid() {
        return fluid;
    }

}
