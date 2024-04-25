package net.pier.geoe.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;

import java.util.HashMap;
import java.util.Map;

public class GeoeItems
{

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Geothermal.MODID);

    public static final Map<ResourceLocation, RegistryObject<Item>> ORE_CHUNKS = new HashMap<>();

    public static void init() {
        REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());

        REGISTER.register("pipe", () -> new BlockItem(GeoeBlocks.Test.PIPE.get(),new Item.Properties().tab(Geothermal.CREATIVE_TAB)));
        REGISTER.register("test", () -> new BlockItem(GeoeBlocks.Test.TEST.get(),new Item.Properties().tab(Geothermal.CREATIVE_TAB)));

    }
}
