package net.pier.geoe.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.pier.geoe.register.GeoeBlocks;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeoeLootBlock implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {


    private BiConsumer<ResourceLocation, LootTable.Builder> consumer;

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        this.consumer = consumer;

        this.dropItSelf(GeoeBlocks.PIPE.get());
        this.dropItSelf(GeoeBlocks.PRODUCTION_WELL.get());
        this.dropItSelf(GeoeBlocks.INJECTION_WELL.get());
        this.dropItSelf(GeoeBlocks.GLASS.get());
        this.dropItSelf(GeoeBlocks.FRAME.get());
        GeoeBlocks.VALVES_BLOCK.values().forEach(blockRegistryObject -> this.dropItSelf(blockRegistryObject.get()));
    }

    private void dropItSelf(Block block)
    {
        this.register(() -> block, singleItem(block));
    }

    private LootPool.Builder singleItem(ItemLike in)
    {
        return createPoolBuilder()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(in));
    }

    private LootPool.Builder createPoolBuilder()
    {
        return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
    }

    private void register(Supplier<? extends Block> b, LootPool.Builder... pools)
    {
        LootTable.Builder builder = LootTable.lootTable();
        for(LootPool.Builder pool : pools)
            builder.withPool(pool);
        register(b, builder);
    }

    private void register(Supplier<? extends Block> supplier, LootTable.Builder table)
    {
        this.register(supplier.get().getRegistryName(), table);
    }

    private void register(ResourceLocation name, LootTable.Builder table)
    {
        ResourceLocation loc = toTableLoc(name);
        consumer.accept(loc, table.setParamSet(LootContextParamSets.BLOCK));
    }

    private ResourceLocation toTableLoc(ResourceLocation in)
    {
        return new ResourceLocation(in.getNamespace(), "blocks/"+in.getPath());
    }
}
