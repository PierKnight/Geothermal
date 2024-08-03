package net.pier.geoe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.block.EnumPipeConnection;
import net.pier.geoe.block.GeothermalPipeBlock;
import net.pier.geoe.client.sound.SoundManager;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class PipeBlockEntity extends BaseBlockEntity {

    private final EnumPipeConnection[] connection = new EnumPipeConnection[6];
    private UUID networkUUID;
    int tankConnections = 0;
    private FluidStack fluidStack = FluidStack.EMPTY;

    public PipeBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(GeoeBlocks.PIPE_BE.get(), pPos, pBlockState);
        Arrays.fill(connection,EnumPipeConnection.NONE);
    }

    public UUID getNetworkUUID() {
        return networkUUID;
    }
    public void setNetworkUUID(UUID uuid) {
        this.networkUUID = uuid;
        this.setChanged();
    }

    public EnumPipeConnection getConnection(Direction direction)
    {
        return connection[direction.get3DDataValue()];
    }

    public void setConnection(Direction direction, EnumPipeConnection connection)
    {

        if(this.connection[direction.get3DDataValue()] == EnumPipeConnection.OUTPUT) tankConnections -= 1;
        this.connection[direction.get3DDataValue()] = connection;
        if(connection == EnumPipeConnection.OUTPUT) tankConnections += 1;
        if(tankConnections == 0)
        {
            this.fluidStack = FluidStack.EMPTY;
            this.syncInfo();
        }
    }

    public int getTankConnections()
    {
        return tankConnections;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    @Override
    public void readTag(CompoundTag tag) {
        if(tag.contains("network"))
            this.networkUUID = tag.getUUID("network");

        ListTag listTag = tag.getList("connections",10);
        for (int i = 0; i < listTag.size(); i++)
            this.connection[i] = EnumPipeConnection.indexOf(listTag.getCompound(i).getByte("connectionType"));
        this.tankConnections = tag.getInt("tankConnections");

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
        tag.putInt("tankConnections",this.tankConnections);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {

        CompoundTag tag = new CompoundTag();
        this.fluidStack.writeToNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.fluidStack = FluidStack.loadFluidStackFromNBT(tag);


        GeothermalPipeBlock.PROPERTY_BY_DIRECTION.forEach((direction, enumPipeConnectionEnumProperty) -> {
            if (!fluidStack.getFluid().getAttributes().isGaseous())
                SoundManager.stopGasLeak(getBlockPos(), direction);
            else if(this.getBlockState().getValue(enumPipeConnectionEnumProperty) == EnumPipeConnection.OUTPUT)
                SoundManager.playGasLeak(getBlockPos(), direction);
        });
    }

    public void checkForFluidUpdate(FluidStack fluidStack)
    {
        if (this.tankConnections > 0 && !fluidStack.getFluid().equals(this.fluidStack.getFluid())) {
            this.fluidStack = fluidStack.copy();
            this.syncInfo();
        }
    }

}
