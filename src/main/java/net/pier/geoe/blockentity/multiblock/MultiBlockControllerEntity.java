package net.pier.geoe.blockentity.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.blockentity.BaseBlockEntity;
import net.pier.geoe.gui.GeoeContainerMenu;
import net.pier.geoe.gui.MenuContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MultiBlockControllerEntity<T extends IMultiBlock> extends BaseBlockEntity
{

    private boolean isComplete = false;

    private final T multiBlock;

    protected final Direction direction;

    public MultiBlockControllerEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, T multiBlock)
    {
        super(pType, pPos, pBlockState);
        this.multiBlock = multiBlock;
        this.direction = pBlockState.getValue(ControllerBlock.FACING);


    }

    public T getMultiBlock() {
        return multiBlock;
    }


    public abstract LazyOptional<Object>[] getHandlers();

    public void onAssemble(Level level){}
    public void onDissemble(Level level){}


    public boolean isComplete() {
        return isComplete;
    }


    public Direction getDirection()
    {
        return this.direction;
    }

    public void destroy()
    {
        if (this.isComplete)
        {
            this.getMultiBlock().disassemble(getLevel(), getBlockPos(), getDirection());
            this.setComplete(false);
        }
    }

    public InteractionResult use(Level level, ServerPlayer serverPlayer)
    {

        if(!this.isComplete && serverPlayer.isCreative() && this.placeStructure(level))
            return InteractionResult.SUCCESS;
        NetworkHooks.openGui(serverPlayer, new SimpleMenuProvider(new MenuConstructor() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
                return MultiBlockControllerEntity.this.getMenu(new MenuContext<BlockEntity>(pContainerId, pPlayerInventory, MultiBlockControllerEntity.this));
            }
        }, Component.nullToEmpty("block." + this.getType().getRegistryName())), getBlockPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(Level level, BlockPos blockPos) {
        if(!level.isClientSide && level.getGameTime() % 10 == 0)
        {
            if(getMultiBlock().checkStructure(level, blockPos, getDirection())) {
                if (!isComplete) {
                    getMultiBlock().assemble(level, blockPos, getDirection());
                    setComplete(true);
                    onAssemble(level);
                }
            }
            else if(isComplete) {
                setComplete(false);
                onDissemble(level);
                getMultiBlock().disassemble(level, blockPos, getDirection());
            }
        }

    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, MultiBlockControllerEntity<?> be) {
        be.tick(level, blockPos);
    }


    @Override
    public void readTag(CompoundTag tag)
    {
        this.isComplete = tag.getBoolean("complete");
        this.getMultiBlock().readFromTag(tag);
    }

    @Override
    public void writeTag(CompoundTag tag)
    {
        tag.putBoolean("complete",this.isComplete);
        this.multiBlock.writeToTag(tag);
    }

    private void setComplete(boolean complete)
    {
        this.isComplete = complete;
        this.setChanged();
        this.syncInfo();
    }

    private boolean placeStructure(Level level)
    {
        if(this.multiBlock instanceof TemplateMultiBlock templateMultiBlock) {
            var structureBlocks = templateMultiBlock.getStructure(level).getStructureBlockInfos();
            for (StructureTemplate.StructureBlockInfo blockInfo : structureBlocks) {
                level.setBlock(getAbsolutePos(blockInfo.pos), blockInfo.state, 3);
            }
            return true;
        }
        return false;
    }

    public BlockPos getAbsolutePos(BlockPos relativePos)
    {
        return this.getMultiBlock().getOffsetPos(level, relativePos, this.direction).offset(this.getBlockPos());
    }

    @Override
    public AABB getRenderBoundingBox() {

        if(getMultiBlock() == null || level == null || !(this.level.getBlockState(getBlockPos()).getBlock() instanceof ControllerBlock<?>))
            return super.getRenderBoundingBox();

        Vec3i size = getMultiBlock().getSize(level);
        Direction direction = getDirection();
        BlockPos min = getMultiBlock().getOffsetPos(level, BlockPos.ZERO,direction);
        BlockPos max = getMultiBlock().getOffsetPos(level, new BlockPos(size),direction);
        return new AABB(min,max).move(this.getBlockPos());
    }

    public abstract GeoeContainerMenu<?> getMenu(MenuContext<?> menuContext);


}
