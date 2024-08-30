package net.pier.geoe.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.material.Fluids;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class GeyserFeature extends Feature<GeyserFeature.Configuration>
{
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = GeoeBlocks.GEYSER_WATER.get().defaultBlockState();

    private static final BlockState GEYSERITE = GeoeBlocks.GEYSERITE.get().defaultBlockState();


    public GeyserFeature(Codec<GeyserFeature.Configuration> pCodec)
    {
        super(pCodec);
    }


    @Override
    public boolean place(FeaturePlaceContext<GeyserFeature.Configuration> context)
    {

        //System.out.println(Thread.currentThread().getName());
        BlockPos origin = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        Random random = context.random();
        BlockPos.MutableBlockPos mutableBlockPos = origin.mutable();


        //LakeFeature
        int startY = mutableBlockPos.getY() - 1;
        int startX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(mutableBlockPos.getX()));
        int startZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(mutableBlockPos.getZ()));


        ReservoirCapability cap = context.level().getLevel().getCapability(ReservoirCapability.CAPABILITY).orElse(null);



        Reservoir val = cap.getReservoirWorldInfo(new ChunkPos(context.origin()));


        int xRadius = 3;  // Radius on the X-axis (horizontal)
        int zRadius = 3;  // Radius on the Z-axis (horizontal)

        BlockPos.MutableBlockPos placingPos = new BlockPos.MutableBlockPos();

        Configuration config = context.config();



        int currentLayerIndex = -1;
        for (int i = 0;i < config.getLayers().size() && currentLayerIndex == -1;i++) {
            Configuration.Layer layer = config.layers.get(i);
            if(startY < layer.height)
                currentLayerIndex = i;
        }

        int maxHeight = startY;
        int minHeight = startY;

        int currentXRadius = xRadius + random.nextInt(2) - 1;  // Adding some randomness
        int currentZRadius = zRadius + random.nextInt(2) - 1;  // Adding some randomness

        for (int x = -currentXRadius - 2; x <= currentXRadius + 2; x++) {
            for (int z = -currentZRadius - 2; z <= currentZRadius + 2; z++) {
                int height = Mth.clamp(context.chunkGenerator().getBaseHeight(x + mutableBlockPos.getX(),z + mutableBlockPos.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, worldgenlevel), startY - 5, startY + 5);
                maxHeight = Math.max(maxHeight, height);
                minHeight = Math.min(minHeight, height);
            }
        }


        if(currentLayerIndex == -1)
            currentLayerIndex = 0;

        for (int y = maxHeight; y > worldgenlevel.getMinBuildHeight(); y--) {
            boolean inDepths = y <= minHeight;
            if(inDepths) {
                currentXRadius = xRadius + random.nextInt(2) - 1;  // Adding some randomness
                currentZRadius = zRadius + random.nextInt(2) - 1;  // Adding some randomness
            }

            if(currentLayerIndex < config.getLayers().size() - 1 && y < config.getLayers().get(currentLayerIndex + 1).height)
                currentLayerIndex++;


            for (int x = -currentXRadius - 2; x <= currentXRadius + 2; x++) {
                for (int z = -currentZRadius - 2; z <= currentZRadius + 2; z++) {

                    double ellipseRadius = (double) (x * x) / (currentXRadius * currentXRadius) + (double) (z * z) / (currentZRadius * currentZRadius);
                    int placingX = mutableBlockPos.getX() + x;
                    int placingZ = mutableBlockPos.getZ() + z;
                    placingPos.set(placingX, y, placingZ);
                    if (ellipseRadius <= 1.0) {

                        if(inDepths)
                            safeSetBlock(worldgenlevel, placingPos, WATER, this::safeReplace);
                        else
                            safeSetBlock(worldgenlevel, placingPos, y < worldgenlevel.getSeaLevel() ? WATER : AIR, this::safeAirReplace);

                        for(Direction direction : Direction.values())
                        {
                            placingPos.set(placingX, y, placingZ);
                            placingPos.setWithOffset(placingPos, direction);

                            if(placingPos.getY() < minHeight) {
                                float geyseriteProb = 1.0F - Math.min(10,minHeight - placingPos.getY()) / 10.0F;
                                BlockStateProvider borderProvider = calc(config, currentLayerIndex, y, random);
                                if(random.nextFloat() <= geyseriteProb)
                                    this.safeSetBlock(worldgenlevel, placingPos, GEYSERITE, this::safeAirReplace);
                                else
                                    this.safeSetBlock(worldgenlevel, placingPos, borderProvider.getState(random, placingPos), this::safeAirReplace);
                            }
                            else
                                this.safeSetBlock(worldgenlevel, placingPos, GEYSERITE, (blockState -> !blockState.isAir() && safeAirReplace(blockState)));
                        }

                        if(placingPos.getY() >= startY)
                        {
                            //safeSetBlock(worldgenlevel, placingPos.relative(Direction.UP), AIR, this::safeAirReplace);
                            //safeSetBlock(worldgenlevel, placingPos.relative(Direction.UP,2), AIR, this::safeAirReplace);
                            //markAboveForPostProcessing(worldgenlevel, placingPos);
                        }
                    }

                }
            }

            if(inDepths) {
                // Occasionally shift the center of the hole slightly to create an imperfect vertical descent
                mutableBlockPos.move(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);

                //make sure the center of the layer doesn't go outside the chunk, to avoid placing blocks in illegal chunks.
                mutableBlockPos.setX(Mth.clamp(mutableBlockPos.getX(), startX, startX + 16));
                mutableBlockPos.setZ(Mth.clamp(mutableBlockPos.getZ(), startZ, startZ + 16));
            }

        }


        return true;
    }

    private BlockStateProvider calc(Configuration configuration, int layerIndex, int height, Random random)
    {
        Configuration.Layer layer = configuration.getLayers().get(layerIndex);
        if(layerIndex < configuration.getLayers().size() - 1) {
            Configuration.Layer nextLayer = configuration.getLayers().get(layerIndex + 1);
            int dif = height - nextLayer.height;
            float prob = Math.min(dif, 10.0F) / 10.0F;
            layer = random.nextFloat() < prob ? layer : nextLayer;
        }
        return layer.stateProvider;
    }

    private boolean safeReplace(BlockState pState) {
        return !pState.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    private boolean safeAirReplace(BlockState pState) {
        return safeReplace(pState) && !pState.getMaterial().isLiquid();
    }

    public static class Configuration implements FeatureConfiguration
    {;


        public static final Codec<Layer> LAYER_CODEC = RecordCodecBuilder.create((builder) -> {
            return builder.group(BlockStateProvider.CODEC.fieldOf("block").forGetter(Layer::stateProvider),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("height").forGetter(Layer::height)).apply(builder, Layer::new);
        });
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create((builder) -> {
            return builder.group(layerOrder().fieldOf("layers").forGetter(Configuration::getLayers)).apply(builder, Configuration::new);
        });
        private final List<Layer> layers;

        public Configuration(List<Layer> layers) {
            this.layers = layers;
        }

        public Configuration(Layer... layers) {
            this(Arrays.stream(layers).toList());
        }


        public List<Layer> getLayers() {
            return layers;
        }

        public record Layer(BlockStateProvider stateProvider, int height){}

        static Codec<List<Layer>> layerOrder() {
            Function<List<Layer>, DataResult<List<Layer>>> checker = layers -> {
                if (layers.isEmpty())
                    return DataResult.error("Layers list cannot be empty");
                int previousHeight = layers.get(0).height;
                for (Layer layer : layers) {
                    if (layer.height > previousHeight)
                        return DataResult.error("Layers are out of order!");
                }
                return DataResult.success(layers);
            };
            return LAYER_CODEC.listOf().flatXmap(checker, checker);
        }
    }



}
