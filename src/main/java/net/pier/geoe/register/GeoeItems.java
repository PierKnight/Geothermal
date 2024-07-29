package net.pier.geoe.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;

import java.util.HashMap;
import java.util.Map;

public class GeoeItems
{

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Geothermal.MODID);

    public static final RegistryObject<Item> PIPE = REGISTER.register("pipe", () -> new BlockItem(GeoeBlocks.PIPE.get(),new Item.Properties().tab(Geothermal.CREATIVE_TAB)));
    public static final RegistryObject<Item> TEST = REGISTER.register("test", () -> new BlockItem(GeoeBlocks.TEST.get(),new Item.Properties().tab(Geothermal.CREATIVE_TAB)));

    public static final Map<ResourceLocation, RegistryObject<Item>> ORE_CHUNKS = new HashMap<>();

}
