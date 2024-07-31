package net.pier.geoe.register;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.ExtractorBlock;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.block.GlassBlock;
import net.pier.geoe.blockentity.ExtractorBlockEntity;
import net.pier.geoe.blockentity.PipeBlockEntity;

import java.util.function.Supplier;

public class GeoeBlocks
{

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Geothermal.MODID);
    public static final RegistryObject<Block> EXTRACTOR = registerBlock("extractor", () -> new ExtractorBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)), true);
    public static final RegistryObject<Block> PIPE = registerBlock("geothermal_pipe", () -> new GeothermalPipeBlock(BlockBehaviour.Properties.of(Material.AMETHYST)), true);
    public static final RegistryObject<Block> GLASS = registerBlock("glass", () -> new GlassBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn(GeoeBlocks::never).isRedstoneConductor(GeoeBlocks::never).isSuffocating(GeoeBlocks::never).isViewBlocking(GeoeBlocks::never)), true);

    public static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Geothermal.MODID);
    public static final RegistryObject<BlockEntityType<PipeBlockEntity>> PIPE_BE = BE_REGISTER.register("pipe_type", () -> BlockEntityType.Builder.of(PipeBlockEntity::new, PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_BE = BE_REGISTER.register("extractor_type", () -> BlockEntityType.Builder.of(ExtractorBlockEntity::new,EXTRACTOR.get()).build(null));




    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> supplier, boolean item)
    {
        RegistryObject<Block> object = REGISTER.register(name, supplier);
        if(item)
            GeoeItems.REGISTER.register(name, () -> new BlockItem(object.get(),new Item.Properties().tab(Geothermal.CREATIVE_TAB)));
        return object;
    }

    private static Boolean never(BlockState p_50779_, BlockGetter p_50780_, BlockPos p_50781_, EntityType<?> p_50782_) {
        return false;
    }

    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }
}
