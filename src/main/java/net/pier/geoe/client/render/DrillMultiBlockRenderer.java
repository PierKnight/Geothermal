package net.pier.geoe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.pier.geoe.blockentity.DrillBlockEntity;
import net.pier.geoe.blockentity.multiblock.IMultiBlock;
import net.pier.geoe.register.GeoeBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class DrillMultiBlockRenderer extends TemplateMultiBlockRenderer<DrillBlockEntity> {
    public DrillMultiBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderMultiBlock(Level level, DrillBlockEntity blockEntity, IMultiBlock iMultiBlock, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        if(blockEntity.getDrillLength() <= 0) {
            return;
        }
        BlockPos.MutableBlockPos drillPos = new BlockPos.MutableBlockPos();
        drillPos.set(iMultiBlock.getOffsetPos(level, DrillBlockEntity.DRILL_POS, blockEntity.getDirection()));


        BlockRenderDispatcher blockRenderDispatcher = this.context.getBlockRenderDispatcher();
        BlockState blockstate = GeoeBlocks.DRILL.get().defaultBlockState();
        BakedModel drillModel =  blockRenderDispatcher.getBlockModel(blockstate);


        float movement = Mth.lerp(pPartialTick, blockEntity.prevClientDrillLength, blockEntity.clientDrillLength);
        float dif = blockEntity.getDrillLength() - movement;
        int totalAmount = Mth.ceil(movement);
        float rotation = (blockEntity.time + pPartialTick) * 0.3F;
        for(int depth = 0; depth < totalAmount; depth++) {
            poseStack.pushPose();
            poseStack.translate(drillPos.getX() + 0.5F,drillPos.getY() + dif,drillPos.getZ() + 0.5F);
            poseStack.mulPose(Vector3f.YN.rotation(rotation));
            poseStack.translate(-0.5F,0F,-0.5F);
            BlockPos absoluteDrillPos = drillPos.offset(blockEntity.getBlockPos());
            for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.chunkBufferLayers()) {
                if (ItemBlockRenderTypes.canRenderInLayer(blockstate, type)) {
                    blockRenderDispatcher.getModelRenderer().tesselateBlock(level, drillModel, blockstate, absoluteDrillPos, poseStack, pBufferSource.getBuffer(type), false, new Random(), blockstate.getSeed(absoluteDrillPos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                }
            }
            drillPos.move(Direction.DOWN);

            poseStack.popPose();
        }

    }
}
