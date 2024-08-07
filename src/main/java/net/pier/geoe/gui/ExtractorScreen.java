package net.pier.geoe.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pier.geoe.gui.info.FluidInfoArea;
import net.pier.geoe.gui.info.ReservoirInfoArea;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ExtractorScreen extends GeoeContainerScreen<ExtractorMenu>{

    private FluidInfoArea fluidInfoArea;

    public ExtractorScreen(ExtractorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

    }

    @Override
    protected void init() {
        super.init();

        this.infoAreas.add(
                new FluidInfoArea(this.getMenu().getBlockEntity().tank, new Rect2i(this.leftPos + 100,this.topPos + 100,100,100))
        );

        this.infoAreas.add(new ReservoirInfoArea(new Rect2i(1,1,31,31), this.menu.reservoir, this.menu.containerId));

    }

    @Override
    protected void renderBg(@Nonnull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {


    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        System.out.println(this.menu.reservoir);
    }
}
