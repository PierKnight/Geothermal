package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends BaseBlockEntity
{
    public AABB fluidAABB = null;

    public TestBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(GeoeBlocks.Test.TEST_BE.get(), pPos, pBlockState);
    }

    @Override
    public void readTag(CompoundTag tag)
    {
        tag.putDouble("minX",this.fluidAABB.minX);
        tag.putDouble("minY",this.fluidAABB.minY);
        tag.putDouble("minZ",this.fluidAABB.minZ);
        tag.putDouble("maxX",this.fluidAABB.maxX);
        tag.putDouble("maxY",this.fluidAABB.maxY);
        tag.putDouble("maxZ",this.fluidAABB.maxZ);
    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        if(tag.contains("minX"))
            this.fluidAABB = new AABB(tag.getDouble("minX"), tag.getDouble("minY"), tag.getDouble("minZ"), tag.getDouble("maxX"), tag.getDouble("maxY"), tag.getDouble("maxZ"));
    }


    public void updateStructure()
    {
        if(level == null)
            return;
        //if(level.isClientSide)
        //    return;
        BlockPos.MutableBlockPos minPos = this.getBlockPos().mutable();
        BlockPos.MutableBlockPos maxPos = this.getBlockPos().mutable();

        boolean foundNearBlock = true;

        while(foundNearBlock)
        {
            foundNearBlock = false;

            for(Direction direction : Direction.values())
            {
                if(direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                {
                    BlockPos offsetMin = minPos.relative(direction);
                    if(isValid(level.getBlockState(offsetMin)))
                    {
                        minPos.set(offsetMin);
                        foundNearBlock = true;
                    }
                }
                else
                {
                    BlockPos offsetMax = maxPos.relative(direction);
                    if(isValid(level.getBlockState(offsetMax)))
                    {
                        maxPos.set(offsetMax);
                        foundNearBlock = true;
                    }
                }
            }
        }
        maxPos.move(1,1,1);
        AABB aabb = new AABB(minPos, maxPos);

        if(aabb.getXsize() < 3 || aabb.getYsize() < 3 || aabb.getZsize() < 3)
        {
            System.out.println("INVALID TOO SMALL" + this.getRenderBoundingBox());
            return;
        }

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for(double i = minPos.getX();i < maxPos.getX();i++)
        {
            for(double j = minPos.getY();j < maxPos.getY();j++)
            {
                for(double k = minPos.getZ();k < maxPos.getZ();k++)
                {
                    mutableBlockPos.set(i, j, k);
                    if(!isValid(this.level.getBlockState(mutableBlockPos)))
                    {
                        System.out.println("NOT VALID");
                        return;
                    }
                }
            }
        }

        this.fluidAABB = aabb.inflate(-1);
        this.syncInfo();
    }


    @Override
    public AABB getRenderBoundingBox()
    {
        if(this.fluidAABB == null)
            return super.getRenderBoundingBox();
        return this.fluidAABB;
    }

    private boolean isValid(BlockState state)
    {
        return state.is(GeoeBlocks.Test.TEST.get()) || state.is(Tags.Blocks.STONE);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return super.getUpdatePacket();
    }
}
