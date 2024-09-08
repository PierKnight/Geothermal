package net.pier.geoe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.advancement.triggers.EarthquakeTrigger;
import net.pier.geoe.item.ReservoirMap;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeItems;

import java.util.Set;
import java.util.function.Consumer;

public class GeoeAdvancementProvider extends AdvancementProvider {


    public GeoeAdvancementProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {

        //ROOT
        Advancement root = AdvancementBuilder.root("block/frame",GeoeBlocks.PIPE).frameType(FrameType.GOAL)
                .addCriterion("pickup", InventoryChangeTrigger.TriggerInstance.hasItems(GeoeBlocks.PIPE.get()))
                .save(consumer, this.fileHelper);

        AdvancementBuilder.child("earthquake", GeoeBlocks.GEYSERITE, root).hidden(true).frameType(FrameType.CHALLENGE)
                .addCriterion("", new EarthquakeTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
                .save(consumer, this.fileHelper);

        AdvancementBuilder mapAdvBuilder = AdvancementBuilder.child("reservoir_map", GeoeItems.RESERVOIR_MAP, root);

        for (ReservoirMap.MapType mapType : ReservoirMap.MapType.values()) {
            CompoundTag mapTagCheck = new CompoundTag();
            mapTagCheck.putString("mapType", mapType.name());
            NbtPredicate nbtPredicate = new NbtPredicate(mapTagCheck);
            ItemPredicate predicate = new ItemPredicate(null, Set.of(GeoeItems.RESERVOIR_MAP.get()), MinMaxBounds.Ints.ANY ,MinMaxBounds.Ints.ANY,new EnchantmentPredicate[0],new EnchantmentPredicate[0],null,nbtPredicate);
            mapAdvBuilder.addCriterion(mapType.name(), InventoryChangeTrigger.TriggerInstance.hasItems(predicate));
        }
        mapAdvBuilder.save(consumer, fileHelper);





    }

}
