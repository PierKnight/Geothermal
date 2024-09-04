package net.pier.geoe.register;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.item.ReservoirMap;
import net.pier.geoe.item.WrenchItem;

public class GeoeItems
{

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Geothermal.MODID);

    public static final RegistryObject<Item> WRENCH = REGISTER.register("wrench", WrenchItem::new);
    public static final RegistryObject<Item> RESERVOIR_MAP = REGISTER.register("reservoir_map", ReservoirMap::new);


}
