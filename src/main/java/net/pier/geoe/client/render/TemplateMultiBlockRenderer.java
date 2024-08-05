package net.pier.geoe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.pier.geoe.block.ControllerBlock;
import net.pier.geoe.blockentity.multiblock.IMultiBlock;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.client.FakeWorld;
import net.pier.geoe.client.RenderHelper;
import net.pier.geoe.client.VertexWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class TemplateMultiBlockRenderer<T extends MultiBlockControllerEntity<TemplateMultiBlock>> implements BlockEntityRenderer<T>
{

    private static final VertexWrapper blockVertexWrapper = new VertexWrapper(null,0.33F);

    private static final FakeWorld fakeWorld = new FakeWorld();

    private final BlockEntityRendererProvider.Context context;

    public TemplateMultiBlockRenderer(BlockEntityRendererProvider.Context context)
    {
        this.context = context;
    }

    public void renderMultiBlock(Level level, T blockEntity, IMultiBlock iMultiBlock)
    {

    }

    @Override
    public void render(T pBlockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
    {

        Level level = pBlockEntity.getLevel();
        if(level == null)
            return;

        if(pBlockEntity.isComplete())
            return;

        TemplateMultiBlock templateMultiBlock = pBlockEntity.getMultiBlock();
        BlockState controllerState = level.getBlockState(pBlockEntity.getBlockPos());

        if(templateMultiBlock != null && controllerState.getBlock() instanceof ControllerBlock<?>) {

                if(pBlockEntity.isComplete())
                    this.renderMultiBlock(level, pBlockEntity, templateMultiBlock);

                Direction direction = controllerState.getValue(ControllerBlock.FACING);
                fakeWorld.updateWorld(level, templateMultiBlock);

                templateMultiBlock.forEachBlock(level, direction, structureBlock -> {
                    BlockState state = structureBlock.state;

                    BlockPos offsetPos = templateMultiBlock.getOffsetPos(level, structureBlock.pos, direction);
                    if (state.getRenderShape() == RenderShape.MODEL) {

                        BlockState currentBlock = level.getBlockState(offsetPos.offset(pBlockEntity.getBlockPos()));
                        if(!currentBlock.equals(structureBlock.state) && !structureBlock.state.isAir()) {
                            poseStack.pushPose();
                            poseStack.translate(offsetPos.getX(),offsetPos.getY(),offsetPos.getZ());
                            poseStack.scale(0.5F,0.5F,0.5F);
                            poseStack.translate(0.5F,0.5F,0.5F);
                            RenderType type = RenderHelper.depthTranslucent();
                            BlockRenderDispatcher blockRenderDispatcher = this.context.getBlockRenderDispatcher();
                            BakedModel bakedModel =  blockRenderDispatcher.getBlockModel(state);
                            blockVertexWrapper.setWrapper(pBufferSource.getBuffer(type));
                            blockRenderDispatcher.getModelRenderer().tesselateBlock(fakeWorld,bakedModel, state, structureBlock.pos, poseStack, blockVertexWrapper, false, new Random(), state.getSeed(pBlockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                            poseStack.popPose();
                        }
                    }
                    return true;
                });
            }
        }



        /*
        VertexConsumer squareNoiseBuffer = pBufferSource.getBuffer(RenderType.lightning());

        float sizeSquare = 0.5F;

        int totalGood = 0;
        if(level.getGameTime() % 20 == 0)
            noise = new PerlinSimplexNoise(new SingleThreadedRandomSource(new Random().nextLong() * 1000000L), List.of(-2,-1,0));
        for(int i = 0;i < 32;i++)
        {
            for(int k = 0;k < 32;k++)
            {
                //double value = GeyserFeature.getValue(123554327L, i, k);
                //value = value * value * value * value;


                double value = noise.getValue(i, k, false);
                float red = 0;
                float green = (float) (value + 1D) / 2F;

                if(value >= 0)
                    continue;
                squareNoiseBuffer.vertex(matrix4f, i * sizeSquare, 5, k * sizeSquare).color(red, green, 0F, 1F).endVertex();
                squareNoiseBuffer.vertex(matrix4f, i * sizeSquare, 5, sizeSquare * (k + 1)).color(red, green, 0F, 1F).endVertex();
                squareNoiseBuffer.vertex(matrix4f, sizeSquare * (i + 1), 5, sizeSquare * (k + 1)).color(red, green, 0F, 1F).endVertex();
                squareNoiseBuffer.vertex(matrix4f, sizeSquare * (i + 1), 5, k * sizeSquare).color(red, green, 0F, 1F).endVertex();

                if(green >= 0.8)
                    totalGood++;
            }
        }
        System.out.println(totalGood / (32D * 32));

         */

        /*
        if(pBlockEntity.fluidAABB == null)
            return;

        PoseStack.Pose lastMatrix = poseStack.last();
        Matrix4f matrix4f = lastMatrix.pose();

        Vec3 vec = context.getBlockEntityRenderDispatcher().camera.getPosition();
        Matrix3f normalMatrix = lastMatrix.normal();

        VertexConsumer buffer = pBufferSource.getBuffer(RenderType.translucent());

        ResourceLocation resourceLocation = Fluids.WATER.getAttributes().getStillTexture();

        Fluids.WATER.getAttributes().getColor(level, pBlockEntity.getBlockPos());
        TextureAtlasSprite liquidTexture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourceLocation);


        float minU = liquidTexture.getU0();
        float maxU = liquidTexture.getU1();
        float minV = liquidTexture.getV0();
        poseStack.pushPose();

        AABB relativePos = pBlockEntity.fluidAABB.move(pBlockEntity.getBlockPos().multiply(-1));

        boolean flag = pBlockEntity.fluidAABB.intersects(vec, vec);

        double fraction = (Math.sin((level.getGameTime() + pPartialTick) * 0.1D) + 1D) * 0.5;
        double height = relativePos.maxY * fraction + (1D - fraction) * relativePos.minY;
        int c = (int)relativePos.minZ;

        for (int a = (int) relativePos.minX; a < relativePos.maxX; a++)
        {
            for (int b = (int) relativePos.minY; b < height; b++)
            {
                double h = Math.min(1D, height - b);

                float maxV = liquidTexture.getV(h * 16);
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, maxV,flag,
                        a, b, c,
                        a, b + (float) h, c,
                        a + 1, b + (float) h, c,
                        a + 1, b, c);
            }
        }

        c = (int)relativePos.maxZ;

        for (int a = (int) relativePos.minX; a < relativePos.maxX; a++)
        {
            for (int b = (int) relativePos.minY; b < height; b++)
            {
                double h = Math.min(1D, height - b);
                float maxV = liquidTexture.getV(h * 16);
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, maxV, flag,
                        a + 1, b, c,
                        a + 1, b + (float) h, c,
                        a, b + (float) h, c,
                        a, b, c);
            }
        }

        c = (int)relativePos.minX;

        for (int a = (int) relativePos.minZ; a < relativePos.maxZ; a++)
        {
            for (int b = (int) relativePos.minY; b < height; b++)
            {
                double h = Math.min(1D, height - b);
                float maxV = liquidTexture.getV(h * 16);
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, maxV, flag,
                        c, b, a + 1,
                        c, b + (float) h, a + 1,
                        c, b + (float) h, a,
                        c, b, a);
            }
        }

        c = (int)relativePos.maxX;

        for (int a = (int) relativePos.minZ; a < relativePos.maxZ; a++)
        {
            for (int b = (int) relativePos.minY; b < height; b++)
            {
                double h = b == height - 1 ?  height - b : 1D;
                float maxV = liquidTexture.getV(h * 16);
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, maxV, flag,
                        c, b, a,
                        c, b + (float) h, a,
                        c, b + (float) h, a + 1,
                        c, b, a + 1);
            }
        }

        for (int a = (int) relativePos.minZ; a < relativePos.maxZ; a++)
        {
            for (int b = (int) relativePos.minX; b < relativePos.maxX; b++)
            {
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, liquidTexture.getV1(), flag,

                        b, (float) height, a + 1,
                        b + 1, (float) height, a + 1,
                        b + 1, (float) height, a,
                        b, (float) height, a);
            }
        }

        for (int a = (int) relativePos.minZ; a < relativePos.maxZ; a++)
        {
            for (int b = (int) relativePos.minX; b < relativePos.maxX; b++)
            {
                drawFace(matrix4f, buffer, normalMatrix, pPackedLight, minU, maxU, minV, liquidTexture.getV1(), flag,

                        b, (float) relativePos.minY + 0.01F, a + 1,
                        b + 1, (float) relativePos.minY + 0.01F, a + 1,
                        b + 1, (float) relativePos.minY + 0.01F, a,
                        b, (float) relativePos.minY + 0.01F, a);
            }
        }

        poseStack.popPose();

         */


    public static void drawFace(Matrix4f matrix4f, VertexConsumer buffer, Matrix3f normal, int light, float minU, float maxU, float minV, float maxV, boolean inside, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4)
    {
        //if(!inside)
        {
            buffer.vertex(matrix4f, x1, y1, z1).color(1F, 1F, 1F, 1F).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x2, y2, z2).color(1F, 1F, 1F, 1F).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x3, y3, z3).color(1F, 1F, 1F, 1F).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x4, y4, z4).color(1F, 1F, 1F, 1F).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
        }
        //else
        {
            buffer.vertex(matrix4f, x4, y4, z4).color(1F, 1F, 1F, 1F).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x3, y3, z3).color(1F, 1F, 1F, 1F).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x2, y2, z2).color(1F, 1F, 1F, 1F).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x1, y1, z1).color(1F, 1F, 1F, 1F).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 1.0F).endVertex();
        }
    }

}
