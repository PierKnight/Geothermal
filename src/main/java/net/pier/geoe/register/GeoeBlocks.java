package net.pier.geoe.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.block.TestBlock;
import net.pier.geoe.blockentity.TestBlockEntity;

public class GeoeBlocks
{

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Geothermal.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Geothermal.MODID);

    public static void init() {
        GeoeBlocks.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        GeoeBlocks.BE_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        Test.init();



    }

    public static final class Test {


        public static final RegistryObject<Block> PIPE = REGISTER.register("geothermal_pipe", () -> new GeothermalPipeBlock(BlockBehaviour.Properties.of(Material.AMETHYST)));
        public static final RegistryObject<Block> TEST = REGISTER.register("test", () -> new TestBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)));
        public static final RegistryObject<BlockEntityType<TestBlockEntity>> TEST_BE = BE_REGISTER.register("test_type", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST.get()).build(null));


        public static void init() {}
    }


}
