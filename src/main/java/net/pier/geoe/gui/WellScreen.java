package net.pier.geoe.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.pier.geoe.gui.info.FluidInfoArea;
import net.pier.geoe.gui.info.ReservoirInfoArea;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

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
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
