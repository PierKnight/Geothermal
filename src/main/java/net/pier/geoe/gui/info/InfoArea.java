package net.pier.geoe.gui.info;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class InfoArea {

    protected final Rect2i area;

    protected InfoArea(Rect2i area) {
        this.area = area;
    }

    protected void fillToolTip(List<Component> list)
    {
    }

    public void draw(PoseStack transform, List<Component> list, int mouseX, int mouseY)
    {
        if(this.area.contains(mouseX, mouseY))
            this.fillToolTip(list);
    }

}
