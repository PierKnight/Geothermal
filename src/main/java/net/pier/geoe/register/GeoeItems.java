package net.pier.geoe.register;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GeoeItems
{

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Geothermal.MODID);

    public static final RegistryObject<Item> WRENCH = REGISTER.register("wrench", () -> new Item(new Item.Properties().tab(Geothermal.CREATIVE_TAB).stacksTo(1)));


}
