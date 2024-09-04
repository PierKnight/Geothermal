package net.pier.geoe.capability.reservoir;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ReservoirSampler {


    public final NormalNoise depthNoise;
    public final NormalNoise sizeNoise;

    public final NormalNoise heatNoise;

    private final WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(0L));

    public ReservoirSampler(ServerLevel serverLevel) {
        this.worldgenrandom.setSeed(serverLevel.getSeed());
        PositionalRandomFactory positionalRandomFactory = worldgenrandom.forkPositional();
        this.depthNoise = NormalNoise.create(positionalRandomFactory.fromHashOf("depth"), new NormalNoise.NoiseParameters(-7, 1.0));
        this.sizeNoise = NormalNoise.create(positionalRandomFactory.fromHashOf("size"), new NormalNoise.NoiseParameters(-5, 1.0));
        this.heatNoise = NormalNoise.create(positionalRandomFactory.fromHashOf("heat"), new NormalNoise.NoiseParameters(-7, 1.0));
    }

    public Sample getSample(ServerLevel serverLevel, int chunkX,int chunkZ)
    {
        worldgenrandom.setLargeFeatureSeed(serverLevel.getSeed() + 54354343L, chunkX, chunkZ);
        return new Sample(
                (float) this.depthNoise.getValue(chunkX,0,chunkZ),
                (float) this.sizeNoise.getValue(chunkX,0,chunkZ),
                (float) this.heatNoise.getValue(chunkX,0,chunkZ),
                this.worldgenrandom.nextInt(5) == 0 ? Reservoir.Type.GEOTHERMAL : Reservoir.Type.STEAM
        );
    }

    public record Sample(float depth, float size, float heat, Reservoir.Type type){}
}
