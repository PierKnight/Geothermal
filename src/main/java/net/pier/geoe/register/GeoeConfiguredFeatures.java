package net.pier.geoe.register;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.pier.geoe.Geothermal;
import net.pier.geoe.world.GeyserFeature;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeoeConfiguredFeatures
{

    public static Holder<ConfiguredFeature<GeyserFeature.Configuration, ?>> GEYSER_CONFIGURED;
    public static Holder<PlacedFeature> GEYSER_PLACE;


    public static void init()
    {
        GEYSER_CONFIGURED = FeatureUtils.register(Geothermal.MODID + ":geyser", GeoeFeatures.GEYSER.get(), new GeyserFeature.Configuration(
                new GeyserFeature.Configuration.Layer(BlockStateProvider.simple(Blocks.STONE),64),
                new GeyserFeature.Configuration.Layer(BlockStateProvider.simple(Blocks.DEEPSLATE),0),
                new GeyserFeature.Configuration.Layer(new WeightedStateProvider(new SimpleWeightedRandomList.Builder<BlockState>()
                        .add(Blocks.DEEPSLATE.defaultBlockState(),2)
                        .add(Blocks.MAGMA_BLOCK.defaultBlockState(),1)),-50)
        ));
        GEYSER_PLACE = PlacementUtils.register(Geothermal.MODID + ":geyser",
                GEYSER_CONFIGURED,
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_TOP_SOLID);

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void initBiome(final BiomeLoadingEvent event)
    {
        event.getGeneration().addFeature(GenerationStep.Decoration.RAW_GENERATION, GEYSER_PLACE);
    }

}
