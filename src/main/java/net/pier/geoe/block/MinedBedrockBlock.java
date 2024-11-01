package net.pier.geoe.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class MinedBedrockBlock extends Block {


    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    public MinedBedrockBlock() {
        super(Properties.copy(Blocks.BEDROCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(PIPE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(PIPE);
    }
    @Override
    public Item asItem() {
        return Blocks.BEDROCK.asItem();
    }
}
