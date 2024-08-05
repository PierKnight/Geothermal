package net.pier.geoe.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.pier.geoe.Geothermal;

public class GeoeTags {



    public static class Items
    {
        public static final TagKey<Item> WRENCH = ItemTags.create(new ResourceLocation("forge", "tools/wrench"));

    }

    public static class Blocks
    {
        public static final TagKey<Block> WRENCH_BREAKABLE = BlockTags.create(new ResourceLocation(Geothermal.MODID, "mineable/wrench"));
    }

    public static class Fluids
    {

        private static final TagKey<Fluid> STEAM_TAG = FluidTags.create(new ResourceLocation("forge", "steam"));
        private static final TagKey<Fluid> GEOTHERMAL_TAG = FluidTags.create(new ResourceLocation("forge", "geothermal_fluid"));
    }


    public static boolean isWrench(ItemStack stack)
    {
        return stack.is(GeoeTags.Items.WRENCH);
    }
}
