package net.pier.geoe.tag;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeItems;
import net.pier.geoe.register.GeoeTags;
import org.jetbrains.annotations.Nullable;

public class GeoeItemTags extends ItemTagsProvider {
    public GeoeItemTags(DataGenerator pGenerator, BlockTagsProvider pBlockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, pBlockTagsProvider, Geothermal.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(GeoeTags.Items.WRENCH).add(GeoeItems.WRENCH.get());
    }
}
