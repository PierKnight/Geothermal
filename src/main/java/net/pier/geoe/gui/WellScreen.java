package net.pier.geoe.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.pier.geoe.blockentity.multiblock.TemplateMultiBlock;
import net.pier.geoe.client.FakeWorld;
import net.pier.geoe.client.render.TemplateMultiBlockRenderer;
import net.pier.geoe.gui.info.FluidInfoArea;
import net.pier.geoe.gui.info.ReservoirInfoArea;
import net.pier.geoe.register.GeoeMultiBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Random;

public class WellScreen extends GeoeContainerScreen<WellMenu>{

    private FluidInfoArea fluidInfoArea;

    public WellScreen(WellMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

    }

    @Override
    public void init() {
        super.init();


        ChunkPos chunkPos = new ChunkPos(this.menu.getBlockEntity().getBlockPos());
        this.infoAreas.add(new ReservoirInfoArea(new Rect2i(0,0,21,21), () -> this.menu.reservoir, chunkPos.toLong()));

    }


    @Override
    protected void renderBg(@Nonnull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {


    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        //super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        var cam = Minecraft.getInstance().cameraEntity;


        RenderSystem.enableBlend();




        pPoseStack.pushPose();

        PoseStack.Pose lastMatrix = pPoseStack.last();
        Matrix3f normalMatrix = lastMatrix.normal();
        TemplateMultiBlockRenderer.drawCube(lastMatrix.pose(), buffer.getBuffer(RenderType.translucentMovingBlock()), normalMatrix, LightTexture.FULL_SKY, new AABB(BlockPos.ZERO));


        // Translate to the center of the GUI


        float movement = (float) Math.sin(Minecraft.getInstance().level.getGameTime());

        TemplateMultiBlock.StructureData structureData = GeoeMultiBlocks.INJECTION_WELL.get().getStructure(Minecraft.getInstance().level);

        pPoseStack.translate(this.width / 2D + movement, this.height / 2D, 0);
        pPoseStack.scale(-10F,-10F,-10F);

        // Apply orthographic projection
        //RenderSystem.setProjectionMatrix(createOrthoProjectionMatrix());
        //RenderSystem.applyModelViewMatrix();



        pPoseStack.mulPose(Vector3f.XP.rotationDegrees(-35.264f));
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(45));

        pPoseStack.translate(-structureData.pivot.getX(), -structureData.pivot.getY(), -structureData.pivot.getZ());



        FakeWorld fakeWorld = new FakeWorld();
        if(Minecraft.getInstance().level != null) {
            fakeWorld.updateWorld(Minecraft.getInstance().level, this.menu.getBlockEntity().getMultiBlock());

            for (StructureTemplate.StructureBlockInfo structureBlock : structureData.getStructureBlockInfos()) {


                pPoseStack.pushPose();

                BlockState state = Blocks.TINTED_GLASS.defaultBlockState();

                pPoseStack.translate(structureBlock.pos.getX(), structureBlock.pos.getY(), structureBlock.pos.getZ());
                Minecraft.getInstance().getBlockRenderer().renderBatched(state, structureBlock.pos, fakeWorld, pPoseStack, buffer.getBuffer(RenderType.translucent()),true, new Random(),EmptyModelData.INSTANCE);
                pPoseStack.popPose();


            };
        }

        pPoseStack.popPose();

        buffer.endBatch();


    }

    // Method to create the orthographic projection matrix
    private Matrix4f createOrthoProjectionMatrix() {
        // Define the orthographic boundaries (left, right, bottom, top, near, far)
        return Matrix4f.orthographic(0, this.width, this.height, 0, -1000.0F, 1000.0F);
    }
}
