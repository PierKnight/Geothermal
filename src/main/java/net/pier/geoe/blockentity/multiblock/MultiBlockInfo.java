package net.pier.geoe.blockentity.multiblock;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.network.PacketMultiBlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MultiBlockInfo {






    public static final Map<ResourceLocation, StructureData> MULTIBLOCK_CACHE = new HashMap<>();

    private final ResourceLocation resourceLocation;

    public MultiBlockInfo(ResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }


    public Vec3i getSize(Level level) {
        return this.getStructure(level).structureTemplate().getSize();
    }

    public List<StructureTemplate.StructureBlockInfo> getStructureBlocks(Level level) {
        return getStructureBlocks(getStructure(level).structureTemplate(), structureBlockInfo -> true);
    }

    private static List<StructureTemplate.StructureBlockInfo> getStructureBlocks(StructureTemplate structureTemplate, Predicate<StructureTemplate.StructureBlockInfo> condition) {
        List<StructureTemplate.StructureBlockInfo> list = new ArrayList<>();

        //access widener
        StructureTemplate.Palette palette = structureTemplate.palettes.get(0);
        for (var blockInfo : palette.blocks()) {
            if (condition.test(blockInfo)) {
                list.add(blockInfo);
            }
        }
        return list;
    }

    private Rotation getRotation(Direction direction)
    {
        Direction direc = Direction.NORTH;
        if(direction == direc)
            return Rotation.NONE;
        else if(direction == direc.getClockWise())
            return Rotation.CLOCKWISE_90;
        else if(direction == direc.getCounterClockWise())
            return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.CLOCKWISE_180;
    }

    public BlockPos getPivot(Level level) {
        return this.getStructure(level).pivot;
    }

    public BlockPos getOffsetPos(Level level, BlockPos pos, Direction direction)
    {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotationPivot(this.getPivot(level));
        if(direction != null)
            settings.setRotation(getRotation(direction));
        return StructureTemplate.calculateRelativePosition(settings, pos).offset(this.getPivot(level).multiply(-1));
    }

    public boolean checkStructure(Level level, Direction direction, BlockPos pos)
    {

        for (StructureTemplate.StructureBlockInfo structureBlockInfo : this.getStructureBlocks(level))
        {
            BlockPos worldPos = this.getOffsetPos(level, structureBlockInfo.pos, direction).offset(pos);
            // pos is the base multiblock block (controller), we can safely assume it's the controller TE block
            if (worldPos.equals(pos)) {
                continue;
            }
            if(!structureBlockInfo.state.is(level.getBlockState(worldPos).getBlock())) {
                return false;
            }
        }
        return true;
    }


    public StructureData getStructure(@Nullable Level level)
    {

        StructureData structureData = MULTIBLOCK_CACHE.get(resourceLocation);
        if(structureData == null && level instanceof ServerLevel serverLevel)
        {
            Optional<StructureTemplate> optional = serverLevel.getStructureManager().get(resourceLocation);
            StructureTemplate structureTemplate = optional.orElseThrow(() -> new NoSuchElementException("Template not found"));
            var pivotList = getStructureBlocks(structureTemplate, structureBlockInfo -> structureBlockInfo.state.getBlock() instanceof ControllerBlock<?>);
            if (pivotList.isEmpty())
                throw new IllegalStateException("no pivot in structure, oh man...");
            BlockPos pos = pivotList.get(0).pos;

            structureData = new StructureData(this.resourceLocation, optional.get(), pos);
            MULTIBLOCK_CACHE.put(resourceLocation, structureData);
        }
        return structureData;
    }





    public record StructureData(ResourceLocation resourceLocation, StructureTemplate structureTemplate, BlockPos pivot){

        public CompoundTag writeToTag(CompoundTag tag){
            this.structureTemplate.save(tag);
            tag.put("pivot",NbtUtils.writeBlockPos(pivot));
            tag.putString("resourceLocation", this.resourceLocation.toString());
            return tag;
        }

        public static StructureData readFromTag(CompoundTag tag)
        {
            StructureTemplate structureTemplate = new StructureTemplate();
            structureTemplate.load(tag);
            BlockPos pivot = NbtUtils.readBlockPos(tag.getCompound("pivot"));
            ResourceLocation resourceLocation = new ResourceLocation(tag.getString("resourceLocation"));
            return new StructureData(resourceLocation, structureTemplate, pivot);
        }
    }
}