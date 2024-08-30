package net.pier.geoe.model;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeBlocks;

import java.util.Objects;

public class GeoeItemModelProvider extends ItemModelProvider {


    public GeoeItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Geothermal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }


}
