package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class BaseBlockEntity extends BlockEntity
{
    public BaseBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState)
    {
        super(pType, pPos, pBlockState);
    }


    public abstract void readTag(CompoundTag tag);
    public abstract void writeTag(CompoundTag tag);


    @Override
    public void load(@Nonnull CompoundTag pTag)
    {
        super.load(pTag);
        readTag(pTag);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag)
    {
        super.saveAdditional(pTag);
        this.writeTag(pTag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        super.handleUpdateTag(tag);
        this.readTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        writeTag(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncInfo()
    {
        BlockState state = this.level.getBlockState(this.getBlockPos());
        level.sendBlockUpdated(this.getBlockPos(), state, state, 2);
    }
}
