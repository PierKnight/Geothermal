package net.pier.geoe.model;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.pier.geoe.Geothermal;
import net.pier.geoe.register.GeoeItems;

public class GeoeItemModelProvider extends ItemModelProvider {


    public GeoeItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Geothermal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        withExistingParent(GeoeItems.RESERVOIR_MAP.get().getRegistryName().getPath(), mcLoc("item/filled_map"));

    }


}
