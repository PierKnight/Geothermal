package net.pier.geoe.register;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.world.GeyserFeature;

public class GeoeFeatures
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Geothermal.MODID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GEYSER = FEATURES.register("geyser", () -> new GeyserFeature(NoneFeatureConfiguration.CODEC));


    public static void init()
    {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
