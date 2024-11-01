package net.pier.geoe.tag;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeTags;
import org.jetbrains.annotations.Nullable;

public class GeoeBlockTags extends BlockTagsProvider {

    public GeoeBlockTags(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Geothermal.MODID, existingFileHelper);


    }

    @Override
    protected void addTags() {


        TagsProvider.TagAppender<Block> tagAppender = tag(GeoeTags.Blocks.WRENCH_BREAKABLE)
                .add(GeoeBlocks.PIPE.get())
                .add(GeoeBlocks.GLASS.get())
                .add(GeoeBlocks.FRAME.get())
                .add(GeoeBlocks.RESERVOIR_PIPE.get())
                .add(GeoeBlocks.DRILL.get());

        GeoeBlocks.REGISTER.getEntries().forEach(blockRegistryObject -> {
            if(blockRegistryObject.get() instanceof ControllerBlock<?> controllerBlock)
                tagAppender.add(controllerBlock);
        });

        for (RegistryObject<Block> blockRegistryObject : GeoeBlocks.VALVES_BLOCK.values())
           tagAppender.add(blockRegistryObject.get());



    }
}
