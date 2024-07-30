package net.pier.geoe.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.pier.geoe.blockentity.multiblock.MultiBlockInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

@NonnullDefault
public class FakeWorld implements BlockAndTintGetter {

    private final LevelLightEngine lightEngine;
    private Level level;

    private final BlockState[][][] blockStates;
    private final Vec3i structureSize;

    public FakeWorld(MultiBlockInfo multiBlockInfo, Level level)
    {
        this.lightEngine = new LevelLightEngine(new LightChunkGetter() {
            @Override
            public BlockGetter getChunkForLighting(int pChunkX, int pChunkZ) {
                return FakeWorld.this;
            }

            @Override
            public @NotNull BlockGetter getLevel() {
                return FakeWorld.this;
            }
        }, false, false);
        this.level = level;


        structureSize = multiBlockInfo.getSize(level);
        blockStates = new BlockState[structureSize.getX()][structureSize.getY()][structureSize.getZ()];

        for (StructureTemplate.StructureBlockInfo structureBlock : multiBlockInfo.getStructureBlocks(level)) {
            blockStates[structureBlock.pos.getX()][structureBlock.pos.getY()][structureBlock.pos.getZ()] = structureBlock.state;

            BlockPos relativePos = BlockPos.ZERO;
            if(structureBlock.state.is(Blocks.GOLD_BLOCK))
                relativePos = structureBlock.pos;
        }


    }




    @Override
    public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
        return 15;
    }

    @Override
    public int getLightEmission(BlockPos pPos) {
        return 15;
    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
        return this.level.getBlockTint(pBlockPos,pColorResolver);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {

        if(pos.getX() < 0 || pos.getX() >= this.structureSize.getX() ||
                pos.getY() < 0 || pos.getY() >= this.structureSize.getY() ||
                pos.getZ() < 0 || pos.getZ() >= this.structureSize.getZ())
            return Blocks.AIR.defaultBlockState();

        BlockState state = this.blockStates[pos.getX()][pos.getY()][pos.getZ()];
        return state != null ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pPos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
