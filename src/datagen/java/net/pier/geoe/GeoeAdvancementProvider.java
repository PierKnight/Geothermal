package net.pier.geoe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.item.ReservoirMap;
import net.pier.geoe.register.GeoeItems;

import java.util.Set;
import java.util.function.Consumer;

public class GeoeAdvancementProvider extends AdvancementProvider {


    public GeoeAdvancementProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        super.registerAdvancements(consumer, fileHelper);


        Advancement.Builder mapAdvancementBuilder = Advancement.Builder.advancement().display(GeoeItems.RESERVOIR_MAP.get(),
                new TranslatableComponent("advancements.geoe.map"),
                new TranslatableComponent("advancement.geoe.map.description"),
                null, FrameType.GOAL, true, true, false);

        for (ReservoirMap.MapType mapType : ReservoirMap.MapType.values()) {

            CompoundTag mapTagCheck = new CompoundTag();

            mapTagCheck.putString("mapType", mapType.name());
            NbtPredicate nbtPredicate = new NbtPredicate(mapTagCheck);
            ItemPredicate predicate = new ItemPredicate(null, Set.of(GeoeItems.RESERVOIR_MAP.get()), MinMaxBounds.Ints.ANY ,MinMaxBounds.Ints.ANY,new EnchantmentPredicate[0],new EnchantmentPredicate[0],null,nbtPredicate);

            mapAdvancementBuilder.addCriterion(mapType.name(), InventoryChangeTrigger.TriggerInstance.hasItems(predicate));
        }

        mapAdvancementBuilder.save(consumer, new ResourceLocation(Geothermal.MODID, "prova"), this.fileHelper);
    }
}
