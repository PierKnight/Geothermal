package net.pier.geoe.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pier.geoe.gui.info.InfoArea;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class GeoeContainerScreen< G extends GeoeContainerMenu<?>> extends AbstractContainerScreen<G> {


    protected final List<InfoArea> infoAreas = new LinkedList<>();
    public GeoeContainerScreen(G pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.infoAreas.clear();
    }


    @Override
    public void render(@Nonnull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        this.infoAreas.forEach(infoArea -> infoArea.draw(pPoseStack));

        List<Component> tooltip = new LinkedList<>();

        for (InfoArea infoArea : this.infoAreas) {
            infoArea.draw(pPoseStack);
            infoArea.fillToolTip(tooltip, pMouseX, pMouseY);
        }

        if(!tooltip.isEmpty())
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        else
            this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }


}
