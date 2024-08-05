/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package net.pier.geoe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidStack;

/**
 * Class heavily based from Immersive Engineering  see
 * <a href="https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/client/utils/GuiHelper.java">link</a>
 */

public class GuiHelper
{

    public static TextureAtlasSprite getSprite(ResourceLocation rl)
    {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(rl);
    }

    public static void drawTexturedColoredRect(
            VertexConsumer builder, PoseStack transform,
            float x, float y, float w, float h,
            float r, float g, float b, float alpha,
            float u0, float u1, float v0, float v1
    )
    {
        builder.defaultColor((int)(255*r), (int)(255*g), (int)(255*b), (int)(255*alpha));
        builder.vertex(x, y+h, 0).uv(u0, v1).endVertex();
        builder.vertex(x+w, y+h, 0).uv(u1, v1).endVertex();
        builder.vertex(x+w, y, 0).uv(u1, v0).endVertex();
        builder.vertex(x, y, 0).uv(u0, v0).endVertex();
        builder.unsetDefaultColor();
    }

    public static void drawTexturedRect(VertexConsumer builder, PoseStack transform, int x, int y, int w, int h, float picSize,
                                        int u0, int u1, int v0, int v1)
    {
        drawTexturedColoredRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0/picSize, u1/picSize, v0/picSize, v1/picSize);
    }

    public static void drawRepeatedFluidSpriteGui(MultiBufferSource buffer, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
    {
        RenderType renderType = RenderHelper.getGuiTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer builder = buffer.getBuffer(renderType);
        drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
    }

    public static void drawRepeatedFluidSprite(VertexConsumer builder, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
    {
        TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
        int col = fluid.getFluid().getAttributes().getColor(fluid);
        int iW = sprite.getWidth();
        int iH = sprite.getHeight();
        if(iW > 0&&iH > 0)
            drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH,
                    sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
                    (col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
    }

    public static void drawRepeatedSprite(VertexConsumer builder, PoseStack transform, float x, float y, float w,
                                          float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax,
                                          float r, float g, float b, float alpha)
    {
        int iterMaxW = (int)(w/iconWidth);
        int iterMaxH = (int)(h/iconHeight);
        float leftoverW = w%iconWidth;
        float leftoverH = h%iconHeight;
        float leftoverWf = leftoverW/(float)iconWidth;
        float leftoverHf = leftoverH/(float)iconHeight;
        float iconUDif = uMax-uMin;
        float iconVDif = vMax-vMin;
        for(int ww = 0; ww < iterMaxW; ww++)
        {
            for(int hh = 0; hh < iterMaxH; hh++)
                drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+hh*iconHeight, iconWidth, iconHeight,
                        r, g, b, alpha, uMin, uMax, vMin, vMax);
            drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth, leftoverH,
                    r, g, b, alpha, uMin, uMax, vMin, (vMin+iconVDif*leftoverHf));
        }
        if(leftoverW > 0)
        {
            for(int hh = 0; hh < iterMaxH; hh++)
                drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW, iconHeight,
                        r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, vMax);
            drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW, leftoverH,
                    r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, (vMin+iconVDif*leftoverHf));
        }
    }

    public static void drawSlot(int x, int y, int w, int h, PoseStack transform)
    {
        drawSlot(x, y, w, h, 0xff, transform);
    }

    public static void drawSlot(PoseStack transform, int x, int y, int w, int h, int dark, int main, int light)
    {
        final int minX = x+8-w/2;
        final int minY = y+8-h/2;
        final int maxX = minX+w;
        final int maxY = minY+h;
        GuiComponent.fill(transform, minX, minY-1, maxX, minY, dark);
        GuiComponent.fill(transform, minX-1, minY-1, minX, maxY, dark);
        GuiComponent.fill(transform, minX, minY, maxX, maxY, main);
        GuiComponent.fill(transform, minX, maxY, maxX+1, maxY+1, light);
        GuiComponent.fill(transform, maxX, minY, maxX+1, maxY, light);
    }

    public static void drawSlot(int x, int y, int w, int h, int alpha, PoseStack transform)
    {
        drawSlot(transform, x, y, w, h, (alpha<<24)|0x373737, (alpha<<24)|0x8b8b8b, (alpha<<24)|0xffffff);
    }
}