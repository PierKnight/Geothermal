package net.pier.geoe.model;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.BlockMachineFrame;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.block.ValveBlock;
import net.pier.geoe.register.GeoeBlocks;

public class GeoeBlockStateProvider extends BlockStateProvider {

    public GeoeBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Geothermal.MODID, exFileHelper);

    }

    @Override
    protected void registerStatesAndModels() {

        for (RegistryObject<Block> value : GeoeBlocks.VALVES_BLOCK.values()) {

            Block block = value.get();
            ModelFile modelFile = new ModelFile.ExistingModelFile(modLoc("block/" + block.getRegistryName().getPath()), this.models().existingFileHelper);

            getVariantBuilder(block)
                    .partialState().with(ValveBlock.FACING, Direction.NORTH).modelForState().modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.WEST).modelForState().rotationY(270).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.SOUTH).modelForState().rotationY(180).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.EAST).modelForState().rotationY(90).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.UP).modelForState().rotationX(-90).modelFile(modelFile).addModel()
                    .partialState().with(ValveBlock.FACING, Direction.DOWN).modelForState().rotationX(90).modelFile(modelFile).addModel();
        }

        for (RegistryObject<Block> entry : GeoeBlocks.REGISTER.getEntries()) {
            if(entry.get() instanceof ControllerBlock<?> controllerBlock)
            {
                ModelFile incompleteModelFile = new ModelFile.ExistingModelFile(modLoc(String.format("block/%s", entry.get().getRegistryName().getPath())), this.models().existingFileHelper);
                ModelFile completeModelFile = new ModelFile.ExistingModelFile(modLoc(String.format("block/%s_complete", entry.get().getRegistryName().getPath())), this.models().existingFileHelper);

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    getVariantBuilder(controllerBlock)
                            .partialState().with(ControllerBlock.FACING, direction).with(BlockMachineFrame.COMPLETE, false).modelForState().rotationY((int) -direction.toYRot() + 180).modelFile(incompleteModelFile).addModel()
                            .partialState().with(ControllerBlock.FACING, direction).with(BlockMachineFrame.COMPLETE, true).modelForState().rotationY((int) -direction.toYRot() + 180).modelFile(completeModelFile).addModel();
                }
            }
        }
    }
}
