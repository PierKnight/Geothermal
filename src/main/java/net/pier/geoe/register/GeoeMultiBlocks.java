package net.pier.geoe.register;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.pier.geoe.Geothermal;
import net.pier.geoe.blockentity.multiblock.DynamicMultiBlock;
import net.pier.geoe.blockentity.multiblock.IMultiBlock;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class GeoeMultiBlocks {

    private static final Map<ResourceLocation, Supplier<? extends IMultiBlock>> REGISTRY = new HashMap<>();

    public static final Supplier<TemplateMultiBlock> PRODUCTION_WELL = register("production_well", () -> new TemplateMultiBlock("production_well"));
    public static final Supplier<TemplateMultiBlock> INJECTION_WELL = register("injection_well", () -> new TemplateMultiBlock("injection_well"));
    public static final Supplier<DynamicMultiBlock> TANK = register("tank", () -> new DynamicMultiBlock(new Vec3i(3,3,3), new BlockPos(5,10,5)));


    public static Supplier<? extends IMultiBlock> getMultiBlock(ResourceLocation resourceLocation)
    {
        Supplier<? extends IMultiBlock> multiBlockInfo = REGISTRY.get(resourceLocation);
        if(multiBlockInfo == null)
            throw new NoSuchElementException("No MultiBlock found in registry");
        return multiBlockInfo;
    }

    private static <T extends IMultiBlock> Supplier<T> register(String name, Supplier<T> multiBlock)
    {
        ResourceLocation resourceLocation = new ResourceLocation(Geothermal.MODID, name);
        REGISTRY.put(resourceLocation, multiBlock);
        return multiBlock;
    }

    public static Collection<Supplier<? extends IMultiBlock>> getMultiblocks() {
        return REGISTRY.values();
    }
}
