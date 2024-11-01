package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.pier.geoe.block.DrillBlock;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.capability.reservoir.ReservoirCapability;
import net.pier.geoe.capability.reservoir.ReservoirDigInfo;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;
import net.pier.geoe.register.GeoeBlocks;
import net.pier.geoe.register.GeoeMultiBlocks;

import java.util.Optional;

public class DrillBlockEntity extends MultiBlockControllerEntity<TemplateMultiBlock> {


    public static final BlockPos DRILL_POS = new BlockPos(1, 2, 3);


    public int time = 0;
    public float clientDrillLength = 0;
    public float prevClientDrillLength = 0;

    private int drillLength = 0;

    private int drillDestroyTime;

    public DrillBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(GeoeBlocks.DRILL_BE.get(), pPos, pBlockState, GeoeMultiBlocks.DRILL.get());
    }


    @Override
    public void onDissemble(Level level) {
        this.drillLength = 0;
        this.syncInfo();
        BlockPos drillPos = this.getAbsolutePos(DRILL_POS);
        level.setBlock(drillPos, GeoeBlocks.DRILL.get().defaultBlockState().setValue(DrillBlock.MULTIBLOCK, false), 3);
    }

    @Override
    public void tick(Level level, BlockPos blockPos) {
        super.tick(level, blockPos);

        if(!this.isComplete())
            return;

        if(!level.isClientSide)
        {
            BlockPos drillPos = this.getAbsolutePos(DRILL_POS);

            if(this.drillLength == 0)
            {
                this.checkForDrill(level, drillPos);
            }
            else
            {
                level.getEntities(null,new AABB(drillPos, drillPos)).forEach(entity -> entity.hurt(DamageSource.ANVIL, 10F));
                BlockState blockState = level.getBlockState(drillPos);
                if(!isWorkingDrill(blockState))
                {
                    this.drillLength = 0;
                    this.syncInfo();
                }
            }
        }
        if(level.getGameTime() % 10 == 0)
        {
            if(this.drillLength > 0 && this.clientDrillLength == this.drillLength)
                dig(level);
        }

        if(level.isClientSide)
            this.time++;
        this.prevClientDrillLength = this.clientDrillLength;
        this.clientDrillLength = Math.min(this.clientDrillLength + 0.1F, this.drillLength);

    }

    private boolean digSingleBlock(Level level, BlockPos pos)
    {
        if(level.getBlockState(pos).is(Blocks.BEDROCK))
            return level.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        return level.destroyBlock(pos, true) || level.getBlockState(pos).getMaterial().isReplaceable();
    }

    private boolean dig(Level level)
    {
        if(this.drillLength <= 0 || level.isClientSide)
            return false;
        BlockPos destroyPos = this.getAbsolutePos(DRILL_POS).offset(0, -this.drillLength, 0);
        Optional<ReservoirDigInfo> reservoirDigInfo = level.getCapability(ReservoirCapability.CAPABILITY).lazyMap(e -> e.getReservoir(new ChunkPos(destroyPos))).map(reservoir -> reservoir.getDigInfo(destroyPos));

        boolean outsideWorld = destroyPos.getY() < level.getMinBuildHeight();


        if(++this.drillDestroyTime > 9) {
            this.drillDestroyTime = 0;
            if (outsideWorld || this.digSingleBlock(level, destroyPos)) {
                if(!outsideWorld)
                    level.setBlock(destroyPos, GeoeBlocks.DRILL.get().defaultBlockState().setValue(DrillBlock.MULTIBLOCK, true), 3);
                else
                    reservoirDigInfo.ifPresent(ReservoirDigInfo::dig);
                this.drillLength++;
                this.syncInfo();
                return true;
            }
        }
        else
            this.drillDestroyTime++;
        return false;
    }

    private boolean isWorkingDrill(BlockState blockState){
        return (blockState.is(GeoeBlocks.DRILL.get()) && blockState.getValue(DrillBlock.MULTIBLOCK)) || blockState.is(Blocks.DIAMOND_BLOCK);
    }

    private void checkForDrill(Level level, BlockPos pos)
    {

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(pos);
        int drillLength = 0;
        boolean foundDrillHead = false;
        while(mutableBlockPos.getY() >= level.getMinBuildHeight() && !foundDrillHead)
        {
            BlockState state = level.getBlockState(mutableBlockPos);
            if(state.is(Blocks.DIAMOND_BLOCK)) {
                foundDrillHead = true;
            }
            else {
                if (!state.is(GeoeBlocks.DRILL.get()))
                    return;
            }
            drillLength++;
            mutableBlockPos.move(Direction.DOWN);
        }



        if(!foundDrillHead)
        {
            Optional<ReservoirDigInfo> reservoirDigInfo = level.getCapability(ReservoirCapability.CAPABILITY).lazyMap(e -> e.getReservoir(new ChunkPos(pos))).map(reservoir -> reservoir.getDigInfo(pos));

            if(reservoirDigInfo.isPresent() && reservoirDigInfo.get().getDrill() > 0)
                drillLength += reservoirDigInfo.get().getDrill();
            else
                return;
        }

        int drillEnd = mutableBlockPos.getY() + 1;
        for(int drillHeight = drillEnd; drillHeight <= pos.getY();drillHeight++)
        {
            mutableBlockPos.set(pos.getX(), drillHeight, pos.getZ());
            level.setBlock(mutableBlockPos,  GeoeBlocks.DRILL.get().defaultBlockState().setValue(DrillBlock.MULTIBLOCK, true), 3);
        }

        this.drillLength = drillLength;
        this.syncInfo();
    }


    public int getDrillLength() {
        return drillLength;
    }

    public int getDrillDestroyTime() {
        return drillDestroyTime;
    }

    @Override
    public LazyOptional<Object>[] getHandlers() {
        return new LazyOptional[0];
    }

    @Override
    public GeoeContainerMenu<?> getMenu(MenuContext<?> menuContext) {
        return null;
    }

    @Override
    public void writeTag(CompoundTag tag) {
        super.writeTag(tag);
        tag.putInt("drillLength", this.drillLength);
    }

    @Override
    public void readTag(CompoundTag tag) {
        super.readTag(tag);

        int prevDrillLength = this.drillLength;
        this.drillLength = tag.getInt("drillLength");
        if(prevDrillLength == 0) {
            this.clientDrillLength = this.drillLength;
            this.prevClientDrillLength = this.drillLength;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB aabb = super.getRenderBoundingBox();
        return aabb.expandTowards(0, -this.drillLength, 0);
    }
}
