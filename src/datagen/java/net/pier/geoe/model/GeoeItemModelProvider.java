package net.pier.geoe.model;

import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeBlocks;

import java.io.IOException;
import java.util.Objects;

public class GeoeItemModelProvider extends ItemModelProvider {


    public GeoeItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Geothermal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        GeoeBlocks.REGISTER.getEntries().stream().map(blockRegistryObject -> blockRegistryObject.get().getRegistryName()).filter(Objects::nonNull).forEach(resourceLocation -> {

            String name = resourceLocation.getPath();
            getBuilder(name).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name)));
        });
    }


}
