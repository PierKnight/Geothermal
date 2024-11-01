package net.pier.geoe.capability.reservoir;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
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
import net.pier.geoe.client.sound.SoundManager;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.network.PacketReservoirSync;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class Reservoir implements IFluidTank, IFluidHandler {


    private final ChunkPos chunkPos;
    private final FluidTank inputTank;
    private final FluidTank outputTank;

    private final int capacity;
    private static final int throughput = 100;
    private final int heatFactor;
    private final Type type;

    //EarthQuake
    private List<BreakingBlockReservoir> breakingBlocks = new LinkedList<>();
    private final Map<BlockPos, ReservoirDigInfo> digInfo = new HashMap<>();
    private int earthquakeTime = 0;


    public Reservoir(ChunkPos chunkPos, int capacity,int heatFactor,Type type) {
        this.inputTank = new FluidTank(capacity, type.inputFluid);
        this.outputTank = new FluidTank(capacity);
        this.capacity = capacity;
        this.type = type;
        this.heatFactor = heatFactor;
        this.chunkPos = chunkPos;
    }

    public Reservoir(CompoundTag tag) {
        this.chunkPos = new ChunkPos(tag.getInt("chunkX"),tag.getInt("chunkZ"));
        this.capacity = tag.getInt("capacity");
        this.type = Type.values()[tag.getInt("type")];
        this.inputTank = new FluidTank(capacity, type.inputFluid).readFromNBT(tag.getCompound("inputTank"));
        this.outputTank = new FluidTank(capacity).readFromNBT(tag.getCompound("outputTank"));
        this.heatFactor = tag.getInt("heatFactor");
        ListTag depthListTag = tag.getList("depthMap", 10);

        for (int i = 0; i < depthListTag.size(); i++) {
            CompoundTag depthTag = depthListTag.getCompound(i);
            ReservoirDigInfo reservoirDigInfo = new ReservoirDigInfo();
            reservoirDigInfo.deserializeNBT(depthTag.getCompound("info"));
            this.digInfo.put(NbtUtils.readBlockPos(depthTag.getCompound("pos")), reservoirDigInfo);
        }

    }




    public int getHeatFactor() {
        return heatFactor;
    }

    public int getThroughput() {
        return throughput;
    }

    public Type getType() {
        return type;
    }


    public float getEarthquakeTime()
    {
        if(this.breakingBlocks.isEmpty() && this.earthquakeTime == 0)
            return -1.0F;
        return this.earthquakeTime / 20F;
    }

    public ReservoirDigInfo getDigInfo(BlockPos pos)
    {
        BlockPos digPos = new BlockPos(pos.getX(),0, pos.getZ());
        ReservoirDigInfo reservoirDigInfo = this.digInfo.get(digPos);
        if(reservoirDigInfo == null)
        {
            reservoirDigInfo = new ReservoirDigInfo();
            this.digInfo.putIfAbsent(digPos, reservoirDigInfo);
        }
        return reservoirDigInfo;
    }

    public void update(Level level, ChunkPos chunkPos)
    {

        if(!level.isClientSide) {
            int conversionAmount = 100;

            int amount = this.inputTank.drain(conversionAmount, FluidAction.SIMULATE).getAmount();
            int fillAmount = this.outputTank.fill(new FluidStack(this.type.outputFluid, amount), IFluidHandler.FluidAction.EXECUTE);
            this.inputTank.drain(fillAmount, FluidAction.EXECUTE);

            if (false && this.earthquakeTime == 0 && level.random.nextFloat() <= 1.0F / 1200F)
                startEarthquake(level, chunkPos);
        }

        //EarthQuake Breaking Blocks
        if(level.getGameTime() % 5 == 0) {
            Iterator<BreakingBlockReservoir> iterator = this.breakingBlocks.iterator();

            while (iterator.hasNext()) {
                BreakingBlockReservoir breakingBlock = iterator.next();
                if(level.isClientSide)
                    Minecraft.getInstance().levelRenderer.destroyBlockProgress(breakingBlock.pos.hashCode(), breakingBlock.pos, breakingBlock.progress);
                if (breakingBlock.progress >= 10) {
                    iterator.remove();
                    if (!level.isClientSide)
                        level.destroyBlock(breakingBlock.pos, true);
                }
                breakingBlock.increaseProgress();
            }
        }
        if(!this.breakingBlocks.isEmpty() && this.earthquakeTime < 20)
            earthquakeTime++;
        if(this.breakingBlocks.isEmpty() && this.earthquakeTime > 0)
            earthquakeTime--;
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

    private void startEarthquake(Level level , ChunkPos chunkPos)
    {

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for(int i = 0;i < 16;i++) {
            for (int j = 0; j < 16; j++) {

                int height = level.getMinBuildHeight();
                mutableBlockPos.set(chunkPos.getBlockX(i), height, chunkPos.getBlockZ(j));
                boolean isSolidBlock = false;
                boolean firstFoundBlock = true;
                while (height < level.getMaxBuildHeight()) {
                    mutableBlockPos.setY(height);
                    BlockState state = level.getBlockState(mutableBlockPos);
                    if (state.getBlock().defaultDestroyTime() < 0.0F) {
                        height++;
                        continue;
                    }
                    if (firstFoundBlock) {
                        isSolidBlock = !state.isAir();
                        firstFoundBlock = false;
                    }
                    if (isSolidBlock == state.isAir() && level.random.nextFloat() <= 0.1) {
                        int offsetY = state.isAir() ? -1 : 0;
                        int progressSpeed = Mth.clamp((int) (state.getDestroySpeed(level, mutableBlockPos) / 50.0F * 15F), 1, 15);
                        int delay = level.random.nextInt(40);
                        this.breakingBlocks.add(new BreakingBlockReservoir(new BlockPos(mutableBlockPos).offset(0, offsetY, 0), 0, progressSpeed,delay));
                        isSolidBlock = !state.isAir();
                    }
                    height++;
                }
            }
        }

        PacketManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x,chunkPos.z)), new PacketReservoirSync(chunkPos, this, PacketReservoirSync.Type.UPDATE));

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

    @Override
    public boolean isFluidValid(@NotNull FluidStack stack) {
        return this.type.inputFluid.test(stack);
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
        updated();
        int space = this.getCapacity() - this.getFluidAmount();
        if(!resource.isEmpty())
            resource.setAmount(Math.min(resource.getAmount(), space));
        return this.inputTank.fill(resource,action);
    }



    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        updated();
        if(!resource.isEmpty())
            resource.setAmount(Math.min(throughput, resource.getAmount()));
        return this.outputTank.drain(resource,action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        updated();
        return this.outputTank.drain(Math.min(throughput, maxDrain),action);
    }



    public CompoundTag save(CompoundTag tag)
    {
        tag.putInt("chunkX", this.chunkPos.x);
        tag.putInt("chunkZ", this.chunkPos.z);
        tag.putInt("capacity", this.capacity);
        tag.putInt("heatFactor", this.heatFactor);
        tag.put("inputTank",this.inputTank.writeToNBT(new CompoundTag()));
        tag.put("outputTank",this.outputTank.writeToNBT(new CompoundTag()));
        tag.putInt("type", this.type.ordinal());

        ListTag listTag = new ListTag();
        this.digInfo.forEach((blockPos, digInfo) -> {
            CompoundTag depthTag = new CompoundTag();
            depthTag.put("pos",NbtUtils.writeBlockPos(blockPos));
            depthTag.put("info", digInfo.serializeNBT());
            listTag.add(depthTag);
        });
        tag.put("depthMap", listTag);

        return tag;
    }

    public void writeUpdate(FriendlyByteBuf buf)
    {
        buf.writeCollection(this.breakingBlocks, (buf1, breakingBlockReservoir) -> {
            buf1.writeBlockPos(breakingBlockReservoir.pos);
            buf1.writeVarInt(breakingBlockReservoir.getProgressSteps());
            buf1.writeVarInt(breakingBlockReservoir.getDelay());
        });

        //buf.writeMap(this.digDepth, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeVarInt);
    }

    public void readUpdate(FriendlyByteBuf buf)
    {
        this.breakingBlocks = buf.readList(buf1 -> new BreakingBlockReservoir(buf1.readBlockPos(), 0, buf1.readVarInt(), buf1.readVarInt()));

        if(!this.breakingBlocks.isEmpty())
            SoundManager.playEarthquake(this.chunkPos);

    }

    private void updated()
    {
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
