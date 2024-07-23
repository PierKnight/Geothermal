package net.pier.geoe.register;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.pier.geoe.Geothermal;

import java.util.Random;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeoeConfiguredFeatures
{

    public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> GEYSER_CONFIGURED;
    public static Holder<PlacedFeature> GEYSER_PLACE;


    public static void init()
    {
        GEYSER_CONFIGURED = FeatureUtils.register(Geothermal.MODID + ":geyser", GeoeFeatures.GEYSER.get());
        GEYSER_PLACE = PlacementUtils.register(Geothermal.MODID + ":geyser", GEYSER_CONFIGURED, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR);

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void initBiome(final BiomeLoadingEvent event)
    {
        event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, GEYSER_PLACE);
    }

}
