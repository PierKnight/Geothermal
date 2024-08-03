package net.pier.geoe;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.pier.geoe.loot.GeoeLootProvider;
import net.pier.geoe.model.GeoeBlockStateProvider;
import net.pier.geoe.model.GeoeItemModelProvider;
import net.pier.geoe.tag.GeoeBlockTags;
import net.pier.geoe.tag.GeoeItemTags;

@Mod.EventBusSubscriber(modid = Geothermal.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataRegister {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        ExistingFileHelper exHelper = event.getExistingFileHelper();
        DataGenerator gen = event.getGenerator();
        if(event.includeServer())
        {
            BlockTagsProvider blockTags = new GeoeBlockTags(gen, exHelper);
            gen.addProvider(blockTags);
            gen.addProvider(new GeoeItemTags(gen, blockTags, exHelper));
            gen.addProvider(new GeoeLootProvider(gen));
            gen.addProvider(new GeoeItemModelProvider(gen, exHelper));
            gen.addProvider(new GeoeBlockStateProvider(gen, exHelper));
            gen.addProvider(new CTMTextureProvider(gen));
        }
    }
}
