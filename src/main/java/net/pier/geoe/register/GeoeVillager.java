package net.pier.geoe.register;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.item.ReservoirMap;

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
            trades.get(1).add(new ItemsForEmeralds(Blocks.MAGMA_BLOCK, 5, 15, 20,1));
            trades.get(1).add(new ItemsForEmeralds(Blocks.DIORITE, 4, 30, 20,1));
            trades.get(2).add(new ItemsForEmeralds(Blocks.OBSIDIAN, 3, 5, 20,1));
            trades.get(2).add(new EmeraldForItems(GeoeBlocks.GEYSERITE.get(), 1, 4, 20,1));
            trades.get(2).add(new ItemsForEmeralds(GeoeBlocks.GEYSERITE.get(), 4, 4, 12,3));
            trades.get(3).add((pTrader, pRandom) -> {
                ReservoirMap.MapType randomType = ReservoirMap.MapType.values()[pRandom.nextInt(ReservoirMap.MapType.values().length)];
                ItemStack inputMap = new ItemStack(GeoeItems.RESERVOIR_MAP.get());
                ItemStack outputMap = new ItemStack(GeoeItems.RESERVOIR_MAP.get());
                ReservoirMap.setReservoirMapType(outputMap, randomType);
                return new MerchantOffer(new ItemStack(Items.EMERALD), inputMap, outputMap, 100, 1,1);
            });
        }
    }

    static class EmeraldForItems implements VillagerTrades.ItemListing {
        private final Item item;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        private final int emeralds;

        public EmeraldForItems(ItemLike pItem, int emeralds, int pCost, int pMaxUses, int pVillagerXp) {
            this.item = pItem.asItem();
            this.cost = pCost;
            this.maxUses = pMaxUses;
            this.villagerXp = pVillagerXp;
            this.priceMultiplier = 0.05F;
            this.emeralds = emeralds;
        }

        public MerchantOffer getOffer(Entity pTrader, Random pRand) {
            ItemStack itemstack = new ItemStack(this.item, this.cost);
            return new MerchantOffer(itemstack, new ItemStack(Items.EMERALD, this.emeralds), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class ItemsForEmeralds implements VillagerTrades.ItemListing {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int numberOfItems;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public ItemsForEmeralds(Block pBlock, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(new ItemStack(pBlock), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
        }

        public ItemsForEmeralds(Item pItem, int pEmeraldCost, int pNumberOfItems, int pVillagerXp) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, 12, pVillagerXp);
        }

        public ItemsForEmeralds(Item pItem, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
        }

        public ItemsForEmeralds(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
            this(pItemStack, pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, 0.05F);
        }

        public ItemsForEmeralds(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp, float pPriceMultiplier) {
            this.itemStack = pItemStack;
            this.emeraldCost = pEmeraldCost;
            this.numberOfItems = pNumberOfItems;
            this.maxUses = pMaxUses;
            this.villagerXp = pVillagerXp;
            this.priceMultiplier = pPriceMultiplier;
        }

        public MerchantOffer getOffer(Entity pTrader, Random pRand) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(this.itemStack.getItem(), this.numberOfItems), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

}
