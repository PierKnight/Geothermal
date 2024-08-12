package net.pier.geoe.capability.reservoir;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.network.PacketBreakingBlocks;
import net.pier.geoe.network.PacketManager;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Reservoir implements IFluidTank, IFluidHandler {

    private final FluidTank inputTank;
    private final FluidTank outputTank;

    private final int capacity;
    private final int throughput;
    private final int temperature;
    private final Type type;

    private final List<BreakingBlockReservoir> breakingBlocks = new LinkedList<>();


    public Reservoir(int capacity, int throughput,int temperature,Type type) {
        this.inputTank = new FluidTank(capacity, type.inputFluid);
        this.outputTank = new FluidTank(capacity);
        this.capacity = capacity;
        this.throughput = throughput;
        this.type = type;
        this.temperature = temperature;
    }

    public Reservoir(CompoundTag tag) {
        this.capacity = tag.getInt("capacity");
        this.type = Type.values()[tag.getInt("type")];
        this.inputTank = new FluidTank(capacity, type.inputFluid).readFromNBT(tag.getCompound("inputTank"));
        this.outputTank = new FluidTank(capacity).readFromNBT(tag.getCompound("outputTank"));
        this.throughput = tag.getInt("throughput");
        this.temperature = tag.getInt("temperature");
    }



    @NotNull
    @Override
    public FluidStack getFluid() {
        return this.outputTank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return this.inputTank.getFluidAmount() + this.outputTank.getFluidAmount();
    }

    @NotNull
    public FluidStack getInput() {
        return this.inputTank.getFluid();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getThroughput() {
        return throughput;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean isFluidValid(@NotNull FluidStack stack) {
        return this.type.inputFluid.test(stack);
    }

    public void update(Level level, ChunkPos chunkPos)
    {
        int conversionAmount = 100;

        int amount = this.inputTank.drain(conversionAmount, FluidAction.SIMULATE).getAmount();
        int fillAmount = this.outputTank.fill(new FluidStack(this.type.outputFluid,amount), IFluidHandler.FluidAction.EXECUTE);
        this.inputTank.drain(fillAmount, FluidAction.EXECUTE);



        /*
        if(level.getGameTime() % 20 == 0 && this.breakingBlocks.isEmpty())
            for(int i = 0;i < 16;i++){
                for(int j = 0;j < 16;j++)
                {
                    double distance = Math.pow(i - 7, 2) + Math.pow(j - 7, 2);
                    double probability = 1D - distance / 128D;
                    if(level.random.nextDouble() < 0.4F)
                        updateEarthQuake(level, chunkPos.getBlockX(i),chunkPos.getBlockZ(j));
                        //collapseChunk(level, chunkPos.getBlockX(i),chunkPos.getBlockZ(j));
                }
            }

         */



        if(level.getGameTime() % 5 == 0) {
            Iterator<BreakingBlockReservoir> iterator = this.breakingBlocks.iterator();

            if (!this.breakingBlocks.isEmpty())
                PacketManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z)), new PacketBreakingBlocks(this.breakingBlocks));


            while (iterator.hasNext()) {
                BreakingBlockReservoir breakingBlock = iterator.next();
                if (breakingBlock.progress >= 10) {
                    iterator.remove();
                    level.destroyBlock(breakingBlock.pos,true);
                }
                else
                    breakingBlock.increaseProgress();
            }
        }
    }

    private void collapseChunk(Level level, int x, int z)
    {

        int height = level.getMinBuildHeight();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, height, z);
        boolean blockBroken = false;
        while (height < level.getMaxBuildHeight())
        {
            mutableBlockPos.setY(height);
            if(level.getBlockState(mutableBlockPos).getBlock().defaultDestroyTime() < 0.0F){
                height++;
                continue;
            }
            if(!blockBroken)
            {
                level.destroyBlock(mutableBlockPos, true);
                blockBroken = true;
            }

            mutableBlockPos.setY(height + 1);
            BlockState state = level.getBlockState(mutableBlockPos);
            //if(state.isAir())
            //    break;
            BlockEntity blockEntity = level.getBlockEntity(mutableBlockPos);
            CompoundTag blockTag = blockEntity != null ? blockEntity.saveWithFullMetadata() : null;


            level.removeBlockEntity(mutableBlockPos);
            level.removeBlock(mutableBlockPos, false);
            mutableBlockPos.setY(height);

            level.setBlock(mutableBlockPos, state,3);
            BlockEntity newBlockEntity = level.getBlockEntity(mutableBlockPos);
            if(newBlockEntity != null && blockTag != null)
                newBlockEntity.load(blockTag);
            height++;
        }
    }

    private void updateEarthQuake(Level level , int x, int z)
    {
        int height = level.getMinBuildHeight();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, height, z);
        boolean isSolidBlock = false;
        boolean firstFoundBlock = true;
        while (height < level.getMaxBuildHeight())
        {
            mutableBlockPos.setY(height);
            BlockState state = level.getBlockState(mutableBlockPos);
            if(state.getBlock().defaultDestroyTime() < 0.0F){
                height++;
                continue;
            }
            if(firstFoundBlock)
            {
                isSolidBlock = !state.isAir();
                firstFoundBlock = false;
            }
            if(isSolidBlock == state.isAir()) {
                int offsetY = state.isAir() ? -1 : 0;
                int progressSpeed = Mth.clamp((int)(state.getDestroySpeed(level, mutableBlockPos) / 50.0F * 15F), 1, 15);
                this.breakingBlocks.add(new BreakingBlockReservoir(new BlockPos(mutableBlockPos).offset(0, offsetY, 0), 0, progressSpeed));
                isSolidBlock = !state.isAir();
            }
            height++;
        }
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? this.inputTank.getFluid() : this.outputTank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.capacity - tank == 0 ? this.outputTank.getFluidAmount() : this.inputTank.getFluidAmount();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 ? this.type.inputFluid.test(stack) : this.type.outputFluid.isSame(stack.getFluid());
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action){

        int space = this.getCapacity() - this.getFluidAmount();
        if(!resource.isEmpty())
            resource.setAmount(Math.min(resource.getAmount(), space));
        return this.inputTank.fill(resource,action);
    }



    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if(!resource.isEmpty())
            resource.setAmount(Math.min(this.throughput, resource.getAmount()));
        return this.outputTank.drain(resource,action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return this.outputTank.drain(Math.min(this.throughput, maxDrain),action);
    }



    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("capacity", this.capacity);
        tag.putInt("temperature", this.temperature);
        tag.putInt("throughput", this.throughput);
        tag.put("inputTank",this.inputTank.writeToNBT(new CompoundTag()));
        tag.put("outputTank",this.outputTank.writeToNBT(new CompoundTag()));
        tag.putInt("type", this.type.ordinal());
        return tag;
    }




    public enum Type{
        STEAM(100, 100,Fluids.LAVA),
        GEOTHERMAL( 100, 100,Fluids.LAVA);

        public final Fluid outputFluid;
        public final Predicate<FluidStack> inputFluid;
        public final int inputAmount;
        public final int outputAmount;

        Type(Fluid outputFluid,Predicate<FluidStack> inputFluid, int inputAmount, int outputAmount) {
            this.inputFluid = inputFluid;
            this.inputAmount = inputAmount;
            this.outputAmount = outputAmount;
            this.outputFluid = outputFluid;
        }

        Type( int inputAmount, int outputAmount, Fluid outputFluid)
        {
            this(outputFluid,fluidStack -> fluidStack.getFluid().is(FluidTags.WATER),inputAmount,outputAmount);
        }
    }


}
