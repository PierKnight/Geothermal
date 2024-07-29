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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.network.PacketManager;
import net.pier.geoe.network.PacketMultiBlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MultiBlockInfo {






    private static final Map<ResourceLocation, MultiBlockInfo> MULTIBLOCKS = new HashMap<>();

    private final StructureTemplate template;
    private final BlockPos pivot;
    private final ResourceLocation resourceLocation;

    private MultiBlockInfo(ResourceLocation resourceLocation, StructureTemplate template)
    {
        this.template = template;
        this.resourceLocation = resourceLocation;

        var pivotList = this.getStructureBlocks(structureBlockInfo -> structureBlockInfo.state.getBlock() instanceof ControllerBlock<?>);
        if (pivotList.isEmpty()) {
            throw new IllegalStateException("no pivot in structure, oh man...");
        }

        this.pivot = pivotList.get(0).pos;
    }


    public MultiBlockInfo(CompoundTag tag)
    {
        this.template = new StructureTemplate();
        this.template.load(tag);
        this.pivot = NbtUtils.readBlockPos(tag.getCompound("pivot"));
        this.resourceLocation = new ResourceLocation(tag.getString("resourceLocation"));
    }

    public Vec3i getSize() {
        return this.template.getSize();
    }

    public List<StructureTemplate.StructureBlockInfo> getStructureBlocks() {
        return getStructureBlocks(structureBlockInfo -> true);
    }

    public List<StructureTemplate.StructureBlockInfo> getStructureBlocks(Predicate<StructureTemplate.StructureBlockInfo> condition) {
        List<StructureTemplate.StructureBlockInfo> list = new ArrayList<>();

        //access widener
        StructureTemplate.Palette palette = template.palettes.get(0);
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

    public BlockPos getPivot() {
        return pivot;
    }

    public BlockPos getOffsetPos(BlockPos pos, Direction direction)
    {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotationPivot(this.pivot);
        if(direction != null)
            settings.setRotation(getRotation(direction));
        return StructureTemplate.calculateRelativePosition(settings, pos).offset(this.pivot.multiply(-1));
    }

    public boolean checkStructure(Level level, Direction direction, BlockPos pos)
    {


        for (StructureTemplate.StructureBlockInfo structureBlockInfo : this.getStructureBlocks())
        {
            BlockPos worldPos = this.getOffsetPos(structureBlockInfo.pos,direction).offset(pos);
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

    public static MultiBlockInfo getMultiBlockInfo(@Nullable Level level, ResourceLocation resourceLocation)
    {

        MultiBlockInfo multiblock = MULTIBLOCKS.get(resourceLocation);
        if(multiblock == null && level instanceof ServerLevel serverLevel)
        {
            Optional<StructureTemplate> optional = serverLevel.getStructureManager().get(resourceLocation);
            if(optional.isPresent())
            {
                multiblock = new MultiBlockInfo(resourceLocation, optional.get());
                MULTIBLOCKS.put(resourceLocation, multiblock);
                PacketManager.INSTANCE.send(PacketDistributor.ALL.noArg(),new PacketMultiBlockInfo(multiblock));
            }
        }
        return multiblock;
    }



    public static void updateMultiBlocks(Collection<MultiBlockInfo> multiBlockInfos)
    {
        multiBlockInfos.forEach(multiBlockInfo -> MULTIBLOCKS.put(multiBlockInfo.resourceLocation,multiBlockInfo));
    }


    public CompoundTag writeToTag(CompoundTag tag){
        this.template.save(tag);
        tag.put("pivot",NbtUtils.writeBlockPos(pivot));
        tag.putString("resourceLocation", this.resourceLocation.toString());
        return tag;
    }

    public static Map<ResourceLocation, MultiBlockInfo> getMultiblocks() {
        return ImmutableMap.copyOf(MULTIBLOCKS);
    }
}