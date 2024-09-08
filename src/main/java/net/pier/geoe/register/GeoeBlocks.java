package net.pier.geoe.register;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.*;
import net.pier.geoe.blockentity.PipeBlockEntity;
import net.pier.geoe.blockentity.WellBlockEntity;
import net.pier.geoe.blockentity.valve.ValveBlockEntity;

import java.util.function.Supplier;

public class GeoeBlocks
{


    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Geothermal.MODID);

    public static final RegistryObject<Block> PRODUCTION_WELL = registerBlock("production_well", () -> new ProductionWellBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)), true);
    public static final RegistryObject<Block> INJECTION_WELL = registerBlock("injection_well", () -> new InjectionWellBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)), true);
    public static final RegistryObject<Block> PIPE = registerBlock("geothermal_pipe", () -> new GeothermalPipeBlock(BlockBehaviour.Properties.of(Material.AMETHYST)), true);
    public static final RegistryObject<Block> GLASS = registerBlock("glass", () -> new GlassBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn(GeoeBlocks::never).isRedstoneConductor(GeoeBlocks::never).isSuffocating(GeoeBlocks::never).isViewBlocking(GeoeBlocks::never)), true);
    public static final RegistryObject<Block> FRAME = registerBlock("frame", () -> new BlockMachineFrame(BlockBehaviour.Properties.of(Material.METAL).strength(0.8F).sound(SoundType.METAL)), true);
    public static final RegistryObject<Block> RESERVOIR_PIPE = registerBlock("reservoir_pipe", () -> new ReservoirPipeBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1.2F).sound(SoundType.ANVIL)), true);

    public static final RegistryObject<Block> GEYSER_WATER = registerBlock("geyser_water", GeyserWaterBlock::new, false);



    public static final RegistryObject<Block> GEYSERITE = registerBlock("geyserite", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F)), true);


    //BLOCK ENTITIES
    public static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Geothermal.MODID);
    public static final RegistryObject<BlockEntityType<PipeBlockEntity>> PIPE_BE = BE_REGISTER.register("pipe_type", () -> BlockEntityType.Builder.of(PipeBlockEntity::new, PIPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<WellBlockEntity.Production>> PRODUCTION_WELL_BE = BE_REGISTER.register("production_well_type", () -> BlockEntityType.Builder.of(WellBlockEntity.Production::new, PRODUCTION_WELL.get()).build(null));
    public static final RegistryObject<BlockEntityType<WellBlockEntity.Injection>> INJECTION_WELL_BE = BE_REGISTER.register("injection_well_type", () -> BlockEntityType.Builder.of(WellBlockEntity.Injection::new, INJECTION_WELL.get()).build(null));


    public static final Table<ValveBlockEntity.Type, ValveBlockEntity.Flow, RegistryObject<Block>> VALVES_BLOCK = HashBasedTable.create();
    public static final Table<ValveBlockEntity.Type, ValveBlockEntity.Flow, RegistryObject<BlockEntityType<BlockEntity>>> VALVES_TYPE = HashBasedTable.create();

    static {
        //VALVE_REGISTRATION
        for (ValveBlockEntity.Type type : ValveBlockEntity.Type.values()) {
            for (ValveBlockEntity.Flow flow : ValveBlockEntity.Flow.values()) {
                RegistryObject<Block> valveBlock = registerBlock( type.name + "_" + flow.name + "_valve", () -> new ValveBlock(BlockBehaviour.Properties.of(Material.METAL).strength(0.8F).sound(SoundType.METAL), type, flow), true);
                var valveType = BE_REGISTER.register(type.name + "_" + flow.name + "_valve_type", () -> BlockEntityType.Builder.of((BlockEntityType.BlockEntitySupplier<BlockEntity>) (pPos, pState) -> new ValveBlockEntity(pPos, pState, type, flow), valveBlock.get()).build(null));
                VALVES_BLOCK.put(type, flow, valveBlock);
                VALVES_TYPE.put(type, flow, valveType);
            }
        }
    }


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
