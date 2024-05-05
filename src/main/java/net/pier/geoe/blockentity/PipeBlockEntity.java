package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.capability.world.WorldNetworkCapability;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class PipeBlockEntity extends BaseBlockEntity {

    private EnumPipeConnection[] connection = new EnumPipeConnection[6];
    public UUID networkUUID;
    int tankConnections = 0;

    public PipeBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(GeoeBlocks.Test.PIPE_BE.get(), pPos, pBlockState);
        Arrays.fill(connection,EnumPipeConnection.NONE);
    }

    public UUID getNetworkUUID() {
        return networkUUID;
    }

    public EnumPipeConnection getConnection(Direction direction)
    {
        return connection[direction.get3DDataValue()];
    }

    public void setConnection(Direction direction, EnumPipeConnection connection)
    {
        if(this.connection[direction.get3DDataValue()].isTankConnection()) tankConnections -= 1;
        this.connection[direction.get3DDataValue()] = connection;
        if(connection.isTankConnection()) tankConnections += 1;
    }

    public boolean hasTankConnections()
    {
        return tankConnections > 0;
    }


    @Override
    public void readTag(CompoundTag tag) {
        if(tag.hasUUID("network"))
            this.networkUUID = tag.getUUID("network");

        ListTag listTag = tag.getList("connections",10);
        for (int i = 0; i < listTag.size(); i++)
            this.connection[i] = EnumPipeConnection.indexOf(listTag.getCompound(i).getByte("connectionType"));
        this.tankConnections = tag.getByte("tankConnections");
    }

    @Override
    public void writeTag(CompoundTag tag) {

        if(this.networkUUID != null)
            tag.putUUID("network",this.networkUUID);
        ListTag listTag = new ListTag();
        for(int i = 0;i < 6;i++)
        {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putByte("connectionType",(byte) connection[i].ordinal());
            listTag.add(connectionTag);
        }
        tag.put("connections",listTag);
        tag.putByte("tankConnections",(byte) this.tankConnections);
    }


    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        FluidStack stack = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluid"));
        System.out.println("LOADED " + stack.getFluid().getRegistryName());
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
    }
}
