package net.pier.geoe.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class GeoeMultiBlocks {

    private static final Map<ResourceLocation, MultiBlockInfo> REGISTRY = new HashMap<>();

    public static final MultiBlockInfo EXTRACTOR = register("extractor");


    public static MultiBlockInfo getMultiBlock(ResourceLocation resourceLocation)
    {
        MultiBlockInfo multiBlockInfo = REGISTRY.get(resourceLocation);
        if(multiBlockInfo == null)
            throw new NoSuchElementException("No MultiBlock found in registry");
        return multiBlockInfo;
    }

    private static MultiBlockInfo register(String name)
    {
        ResourceLocation resourceLocation = new ResourceLocation(Geothermal.MODID, name);
        MultiBlockInfo multiBlockInfo = new MultiBlockInfo(resourceLocation);
        REGISTRY.put(resourceLocation, multiBlockInfo);
        return multiBlockInfo;
    }

    public static Collection<MultiBlockInfo> getMultiblocks() {
        return REGISTRY.values();
    }
}
