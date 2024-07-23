package net.pier.geoe.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.ChunkEvent;
import net.pier.geoe.capability.ReservoirCapability;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GeyserFeature extends Feature<NoneFeatureConfiguration>
{
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Fluids.WATER.defaultFluidState().createLegacyBlock();
    private static final Direction[] DIRECTIONS = new Direction[]{Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

    public GeyserFeature(Codec<NoneFeatureConfiguration> pCodec)
    {
        super(pCodec);
    }


    @Override
    public boolean place(NoneFeatureConfiguration pConfig, WorldGenLevel pLevel, ChunkGenerator pChunkGenerator, Random pRandom, BlockPos pOrigin)
    {
        return super.place(pConfig, pLevel, pChunkGenerator, pRandom, pOrigin);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        //System.out.println(Thread.currentThread().getName());
        BlockPos origin = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        Random random = context.random();


        ReservoirCapability cap = context.level().getLevel().getCapability(ReservoirCapability.CAPABILITY).orElse(null);


        float val = cap.getReservoir(context.level().getLevel(),new ChunkPos(context.origin()));


        float a = 1F + random.nextFloat() * 1.5F;
        float b = 2F + random.nextFloat() * 2F;
        float c = 1F + random.nextFloat() * 1.5F;
        float size = 2F + random.nextFloat() * 5F;


        float dimensionA = Mth.ceil(Mth.sqrt(size * a * a)) + 0.5F;
        float dimensionB = Mth.ceil(Mth.sqrt(size * b * b));
        float dimensionC = Mth.ceil(Mth.sqrt(size * c * c)) + 0.5F;



        BlockPos.MutableBlockPos mutableBlockPos = origin.mutable();

        float angle = random.nextFloat() * (float) Math.PI;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);

        float dimension = Math.max(Math.max(dimensionA, dimensionB), dimensionC);
        Set<Long> positions = new HashSet<>();

        for (float i = -dimension; i < dimension; i++)
        {
            for (float j = 0; j < dimension; j++)
            {
                for (float k = -dimension; k < dimension; k++)
                {
                    float x = i * cos - k * sin;
                    float z = i * sin + k * cos;

                    float value = (x * x) / (a * a) + (j * j) / (b * b) + (z * z) / (c * c);
                    if(value <= size)
                    {
                        BlockPos pos = origin.offset(i, -j, k);
                        BlockState state = worldgenlevel.getBlockState(pos);
                        if(!state.is(BlockTags.FEATURES_CANNOT_REPLACE) && !state.isAir() && !state.getMaterial().isLiquid() && isNearSolid(worldgenlevel, pos, mutableBlockPos))
                        {
                            boolean water = pos.getY() < worldgenlevel.getSeaLevel();
                            BlockState placeState = water ? WATER : AIR;
                            worldgenlevel.setBlock(pos, placeState, 2);
                            if(water)
                                worldgenlevel.scheduleTick(pos, placeState.getBlock(), 0);

                            positions.add((((long)pos.getX()) << 32) | (pos.getZ() & 0xffffffffL));
                        }
                    }
                }
            }
        }


        positions.forEach((posV) ->
        {
            mutableBlockPos.set((int)(posV >> 32), 0, posV.intValue());
            mutableBlockPos.setY(worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR, mutableBlockPos.getX(), mutableBlockPos.getZ()) - 1);
            worldgenlevel.setBlock(mutableBlockPos, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        });

        BlockPos pos = worldgenlevel.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, origin).below();
        worldgenlevel.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);

        return false;
    }


    private static boolean isNearSolid(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos directionCheck)
    {

        for (Direction direction : DIRECTIONS)
        {
            directionCheck.setWithOffset(pos, direction);
            if(level.getBlockState(directionCheck).getMaterial().isSolid())
                return true;
        }
        return false;
    }
}
