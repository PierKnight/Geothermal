package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.GameData;
import net.pier.geoe.Geothermal;
import net.pier.geoe.block.BlockMachineFrame;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.block.ValveBlock;
import net.pier.geoe.blockentity.valve.ValveBlockEntity;
import net.pier.geoe.register.GeoeBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class TemplateMultiBlock implements IMultiBlock{




    public static final Map<ResourceLocation, StructureData> MULTIBLOCK_CACHE = new HashMap<>();

    private final ResourceLocation resourceLocation;

    public TemplateMultiBlock(String templateName)
    {
        this.resourceLocation = new ResourceLocation(Geothermal.MODID, templateName);
    }


    public Vec3i getSize(Level level) {
        return this.getStructure(level).size;
    }

    public boolean forEachBlock(Level level, Direction direction, Function<StructureTemplate.StructureBlockInfo, Boolean> consumer) {
        Rotation rotation = IMultiBlock.getRotation(direction);
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : this.getStructure(level).structureBlockInfos) {
            if(!consumer.apply(new StructureTemplate.StructureBlockInfo(structureBlockInfo.pos, structureBlockInfo.state.rotate(rotation), structureBlockInfo.nbt)))
                return false;
        }
        return true;
    }

    public BlockPos getPivot(Level level) {
        return this.getStructure(level).pivot;
    }


    public boolean checkStructure(Level level, BlockPos pos, Direction direction)
    {
        return this.forEachBlock(level, direction, structureBlockInfo -> {
            BlockPos worldPos = this.getOffsetPos(level, structureBlockInfo.pos, direction).offset(pos);
            return IMultiBlock.compareBlockState(structureBlockInfo.state, level.getBlockState(worldPos));
        });
    }

    public void assemble(Level level, BlockPos pos, Direction direction)
    {
        updateFrameBlocks(level, pos, direction, true);
    }

    public void disassemble(Level level, BlockPos pos, Direction direction)
    {
        updateFrameBlocks(level, pos, direction, false);
    }

    private void updateFrameBlocks(Level level, BlockPos pos, Direction direction, boolean complete)
    {
        this.forEachBlock(level, direction, structureBlockInfo -> {
            BlockPos worldPos = this.getOffsetPos(level, structureBlockInfo.pos, direction).offset(pos);
            BlockState state = level.getBlockState(worldPos);
            state.getOptionalValue(BlockMachineFrame.COMPLETE).ifPresent((c) -> level.setBlock(worldPos, state.setValue(BlockMachineFrame.COMPLETE, complete), 3));

            if(state.getBlock() instanceof ValveBlock && level.getBlockEntity(worldPos) instanceof ValveBlockEntity valve)
                valve.updateController(complete ? pos : null, structureBlockInfo.nbt.getInt("index"));
            return true;
        });
    }



    public StructureData getStructure(@Nullable Level level)
    {
        StructureData structureData = MULTIBLOCK_CACHE.get(resourceLocation);
        if(structureData == null && level instanceof ServerLevel serverLevel)
        {
            Optional<StructureTemplate> optional = serverLevel.getStructureManager().get(resourceLocation);
            StructureTemplate structureTemplate = optional.orElseThrow(() -> new NoSuchElementException("Template not found"));
            structureData = new StructureData(this.resourceLocation, structureTemplate.palettes.get(0).blocks(), new BlockPos(structureTemplate.getSize()));
            MULTIBLOCK_CACHE.put(resourceLocation, structureData);
        }
        return structureData;
    }





    public static class StructureData {


        public final ResourceLocation resourceLocation;
        private final List<StructureTemplate.StructureBlockInfo> structureBlockInfos = new LinkedList<>();
        private final BlockPos size;

        public final BlockPos pivot;

        public final BlockState[][][] blockStates;
        public StructureData(ResourceLocation resourceLocation, List<StructureTemplate.StructureBlockInfo> blocks, BlockPos size)
        {
            this.resourceLocation = resourceLocation;

            blockStates = new BlockState[size.getX()][size.getY()][size.getZ()];

            BlockPos foundPivot = null;

            for (var blockInfo : blocks) {
                BlockState blockState = blockInfo.state;
                if(blockState.is(Blocks.STRUCTURE_BLOCK) && blockInfo.nbt != null && blockInfo.nbt.contains("metadata"))
                    blockState = getValveFromString(blockInfo.nbt.getString("metadata"), blockInfo.nbt);
                this.structureBlockInfos.add(new StructureTemplate.StructureBlockInfo(blockInfo.pos, blockState, blockInfo.nbt));
                this.blockStates[blockInfo.pos.getX()][blockInfo.pos.getY()][blockInfo.pos.getZ()] = blockState;
                if(blockState.getBlock() instanceof ControllerBlock<?>)
                    foundPivot = blockInfo.pos;
            }
            if (foundPivot == null)
                throw new IllegalStateException("no pivot in structure, oh man...");
            this.pivot = foundPivot;
            this.size = size;
        }


        private BlockState getValveFromString(String string, CompoundTag tag)
        {
            String[] data = string.split(",");
            if(data.length != 4)
                throw new IllegalArgumentException("Structure Valve Block has not three properties");
            ValveBlockEntity.Type type = ValveBlockEntity.Type.valueOf(data[0]);
            ValveBlockEntity.Flow flow = ValveBlockEntity.Flow.valueOf(data[1]);
            Direction valveDirection = Objects.requireNonNull(Direction.byName(data[2]),"Incorrect Direction Enum");
            tag.putInt("index", Integer.parseInt(data[3]));
            return Objects.requireNonNull(GeoeBlocks.VALVES_BLOCK.get(type, flow)).get().defaultBlockState().setValue(ValveBlock.FACING, valveDirection);
        }

        public void encode(FriendlyByteBuf buf){

            buf.writeResourceLocation(this.resourceLocation);
            buf.writeBlockPos(this.size);
            buf.writeCollection(this.structureBlockInfos, (buf1, structureBlockInfo) -> {
                buf1.writeBlockPos(structureBlockInfo.pos);
                buf1.writeVarInt(GameData.getBlockStateIDMap().getId(structureBlockInfo.state));
                buf1.writeNbt(structureBlockInfo.nbt);
            });
        }

        public static StructureData decode(FriendlyByteBuf buf)
        {
            ResourceLocation resourceLocation = buf.readResourceLocation();
            BlockPos size = buf.readBlockPos();
            List<StructureTemplate.StructureBlockInfo> blocks = buf.readList(buf1 -> {
                BlockPos pos = buf1.readBlockPos();
                BlockState state = GameData.getBlockStateIDMap().byId(buf1.readVarInt());
                CompoundTag tag = buf1.readNbt();
                return new StructureTemplate.StructureBlockInfo(pos, state, tag);
            });
            return new StructureData(resourceLocation, blocks, size);
        }
    }
}