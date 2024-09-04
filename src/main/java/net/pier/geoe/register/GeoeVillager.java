package net.pier.geoe.register;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.item.ReservoirMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class GeoeVillager {

    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, Geothermal.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, Geothermal.MODID);

    private static final RegistryObject<PoiType> GEOLOGIST_POI_TYPE = POI_TYPES.register("geologist_poi", () -> new PoiType("geologist", PoiType.getBlockStates(Blocks.AMETHYST_BLOCK), 1, 1));
    public static final RegistryObject<VillagerProfession> GEOLOGIST = PROFESSIONS.register("geologist", () -> new VillagerProfession("geologist", GEOLOGIST_POI_TYPE.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.STONE_BREAK));


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void customTrades(VillagerTradesEvent event)
    {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        if(event.getType().equals(GEOLOGIST.get()))
        {
            trades.get(1).add(new VillagerTrades.ItemListing() {
                @Nullable
                @Override
                public MerchantOffer getOffer(Entity pTrader, Random pRandom) {
                    ReservoirMap.MapType randomType = ReservoirMap.MapType.values()[pRandom.nextInt(ReservoirMap.MapType.values().length)];
                    ItemStack inputMap = new ItemStack(GeoeItems.RESERVOIR_MAP.get());
                    ItemStack outputMap = new ItemStack(GeoeItems.RESERVOIR_MAP.get());
                    ReservoirMap.setReservoirMapType(outputMap, randomType);
                    return new MerchantOffer(new ItemStack(Items.EMERALD), inputMap, outputMap, 100, 1,1);
                }
            });
        }
    }



}
