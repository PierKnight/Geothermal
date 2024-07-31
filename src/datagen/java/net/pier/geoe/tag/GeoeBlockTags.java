package net.pier.geoe.tag;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeTags;
import org.jetbrains.annotations.Nullable;

public class GeoeBlockTags extends BlockTagsProvider {

    public GeoeBlockTags(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Geothermal.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {


        tag(GeoeTags.Blocks.WRENCH_BREAKABLE)
                .add(GeoeBlocks.PIPE.get())
                .add(GeoeBlocks.EXTRACTOR.get())
                .add(GeoeBlocks.GLASS.get());

    }
}
