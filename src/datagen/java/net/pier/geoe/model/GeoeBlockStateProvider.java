package net.pier.geoe.model;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.*;
import net.pier.geoe.register.GeoeBlocks;

import java.util.Objects;

public class GeoeBlockStateProvider extends BlockStateProvider {

    public GeoeBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Geothermal.MODID, exFileHelper);

    }

    @Override
    protected void registerStatesAndModels() {


        simpleBlock(GeoeBlocks.GEYSERITE.get());

        getVariantBuilder(GeoeBlocks.DRILL.get())
                .partialState().with(DrillBlock.MULTIBLOCK, false).modelForState().modelFile(getModelFileFromBlock(GeoeBlocks.DRILL.get())).addModel()
                .partialState().with(DrillBlock.MULTIBLOCK, true).modelForState().modelFile(getModelFileFromBlock(GeoeBlocks.DRILL.get())).addModel();

        var reservoirPipeBuilder = getVariantBuilder(GeoeBlocks.RESERVOIR_PIPE.get()).partialState();
        for (ReservoirPipeBlock.FluidType fluidType : ReservoirPipeBlock.FluidType.values())
            reservoirPipeBuilder.with(ReservoirPipeBlock.FLUID_TYPE, fluidType).modelForState().modelFile(getModelFileFromBlock(GeoeBlocks.RESERVOIR_PIPE.get())).addModel();



        //all possible valve types
        for (RegistryObject<Block> value : GeoeBlocks.VALVES_BLOCK.values()) {

            Block block = value.get();
            ModelFile modelFile = getModelFileFromBlock(block);

            getVariantBuilder(block)
                    .partialState().with(ValveBlock.FACING, Direction.NORTH).modelForState().modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.WEST).modelForState().rotationY(270).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.SOUTH).modelForState().rotationY(180).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.EAST).modelForState().rotationY(90).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.UP).modelForState().rotationX(-90).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.DOWN).modelForState().rotationX(90).modelFile(modelFile).addModel();
        }

        for (RegistryObject<Block> entry : GeoeBlocks.REGISTER.getEntries()) {



            String name = Objects.requireNonNull(entry.get().getRegistryName()).getPath();

            //controller blockstates
            if(entry.get() instanceof ControllerBlock<?> controllerBlock)
            {
                ModelFile incompleteModelFile = new ModelFile.ExistingModelFile(modLoc(String.format("block/%s", name)), this.models().existingFileHelper);
                ModelFile completeModelFile = new ModelFile.ExistingModelFile(modLoc(String.format("block/%s_complete", name)), this.models().existingFileHelper);

                for (Direction direction : Direction.Plane.HORIZONTAL) {

                    //BlockState
                    getVariantBuilder(controllerBlock)
                            .partialState().with(ControllerBlock.FACING, direction).with(BlockMachineFrame.COMPLETE, false).modelForState().rotationY((int) direction.toYRot() + 180).modelFile(incompleteModelFile).addModel()
                            .partialState().with(ControllerBlock.FACING, direction).with(BlockMachineFrame.COMPLETE, true).modelForState().rotationY((int) direction.toYRot()  + 180).modelFile(completeModelFile).addModel();


                }
            }

            //add item models to any block with a corresponding item block
            if(entry.get().asItem() != Items.AIR)
                itemModels().withExistingParent(name, modLoc("block/" + name));

        }
    }

    private ModelFile getModelFileFromBlock(Block block)
    {
        return new ModelFile.ExistingModelFile(modLoc("block/" + block.getRegistryName().getPath()), this.models().existingFileHelper);
    }

}
