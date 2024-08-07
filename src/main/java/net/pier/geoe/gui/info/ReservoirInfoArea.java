package net.pier.geoe.gui.info;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.capability.world.Reservoir;
import net.pier.geoe.client.RenderHelper;

import java.util.List;
import java.util.Random;

import static net.pier.geoe.client.GuiHelper.*;

public class ReservoirInfoArea extends InfoArea{

    private final Reservoir reservoir;
    private final NormalNoise noise;

    private final float a,b, noiseScale, rotation;

    public ReservoirInfoArea(Rect2i area, Reservoir reservoir, long seed) {
        super(area);
        this.reservoir = reservoir;
        RandomSource randomSource = new XoroshiroRandomSource(seed);

        float size = area.getWidth() * 0.5F;
        float ellipseArea = size * size;
        this.noise = NormalNoise.create(randomSource,new NormalNoise.NoiseParameters(-1,1.0));
        this.a = area.getWidth() * 0.5F;
        this.b = ellipseArea / (this.a);
        this.noiseScale = 0.8F;
        this.rotation = 0;
    }

    private double getPerturbedRadius(double theta) {
        double rOriginal = (a * b) / Math.sqrt(b * b * Math.cos(theta) * Math.cos(theta) + a * a * Math.sin(theta) * Math.sin(theta));
        double noiseValue = this.noise.getValue(Math.cos(theta), Math.sin(theta),0); // Get noise value for the angle
        return rOriginal * (1 - (noiseValue + 1) * 0.5 * noiseScale);
    }

    // Function to check if point (x, y) is inside the perturbed ellipse
    public boolean isInside(double x, double y) {



        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);
        double rPerturbed = getPerturbedRadius(theta);
        return r <= rPerturbed;
    }

    @Override
    public void draw(PoseStack transform) {

        transform.pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        RenderType renderType = RenderHelper.getGuiTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer builder = buffer.getBuffer(renderType);

        TextureAtlasSprite fluidSprite = getSprite(Fluids.WATER.getAttributes().getStillTexture());
        TextureAtlasSprite lavaSprite = getSprite(Fluids.LAVA.getAttributes().getStillTexture());
        TextureAtlasSprite stoneSprite = getSprite(new ResourceLocation("minecraft","block/stone"));
        int col = Fluids.WATER.getAttributes().getColor(new FluidStack(Fluids.WATER,1));
        int areaX = this.area.getX();
        int areaY = this.area.getY();
        int tileSize = 5;
        int minY = Integer.MAX_VALUE;
        int maxY = 0;

        for(int ww = 0;ww < this.area.getWidth();ww++)
        {
            for(int hh = 0;hh < this.area.getHeight();hh++)
            {


                int x = ww - (int)(this.area.getWidth() * 0.5);
                int y = hh - (int)(this.area.getHeight() * 0.5);

                boolean isCave = isInside(x, y);

                if(isCave) {
                    drawTexturedColoredRect(builder, transform, areaX+ww*tileSize, areaY+hh*tileSize, tileSize, tileSize,
                            0.4F,0.4F,0.4F,
                            1, stoneSprite.getU0(), stoneSprite.getU1(), stoneSprite.getV0(), stoneSprite.getV1());
                    minY = Math.min(minY, hh);
                    maxY = Math.max(maxY, hh);
                }
                else
                {
                    drawTexturedColoredRect(builder, transform, areaX+ww*tileSize, areaY+hh*tileSize, tileSize, tileSize,
                            0.7F,0.7F,0.7F,
                            1, stoneSprite.getU0(), stoneSprite.getU1(), stoneSprite.getV0(), stoneSprite.getV1());
                }
            }
        }

        int height = 200;
        int fluidHeight = (int)(height*(0.1));
        drawRepeatedFluidSprite(builder, transform, new FluidStack(Fluids.WATER,1),0,height - fluidHeight,100,fluidHeight);




        /*
        int reservoirHeight = maxY - minY;
        float waterPercentage = 0.254532F;
        float lavaPercentage = 1.0F - waterPercentage;
        int fluidHeight = (int)(reservoirHeight*waterPercentage);
        float remaining = (reservoirHeight * waterPercentage - fluidHeight);
        int lavaFluidHeight = (int)(reservoirHeight*lavaPercentage);
        float lavaRemaining = (reservoirHeight * lavaPercentage - lavaFluidHeight);

        for(int ww = 0;ww < this.area.getWidth();ww++)
        {

            float leftoverH = tileSize * remaining;

            for(int hh = 0;hh < fluidHeight;hh++)
            {
                drawTexturedColoredRect(builder, transform, areaX + ww * tileSize, areaY + (maxY - hh) * tileSize, tileSize, tileSize,
                        (col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f,
                        1, fluidSprite.getU0(), fluidSprite.getU1(), fluidSprite.getV0(), fluidSprite.getV1());
            }



            drawTexturedColoredRect(builder, transform, areaX + ww * tileSize, areaY + (maxY - fluidHeight) * tileSize + (tileSize - leftoverH), tileSize, leftoverH,
                    (col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f,
                    1, fluidSprite.getU0(), fluidSprite.getU1(), fluidSprite.getV0(), fluidSprite.getV1());

            float secondFluidShift = tileSize - leftoverH;
            float leftoverLavaH = tileSize * lavaRemaining;

            for(int hh = 0;hh < lavaFluidHeight + 1;hh++)
            {
                drawTexturedColoredRect(builder, transform, areaX + ww * tileSize, areaY + secondFluidShift + (maxY - fluidHeight - 1 - hh) * tileSize, tileSize, tileSize,
                        1,1,1,
                        0.5F, lavaSprite.getU0(), lavaSprite.getU1(), lavaSprite.getV0(), lavaSprite.getV1());
            }

            drawTexturedColoredRect(builder, transform, areaX + ww * tileSize, areaY + (maxY - fluidHeight - lavaFluidHeight - 1) * tileSize, tileSize, leftoverLavaH,
                    1, 1,1,
                    0.5F, lavaSprite.getU0(), lavaSprite.getU1(), lavaSprite.getV0(), lavaSprite.getV1());

        }

         */

        buffer.endBatch();
        transform.popPose();

    }




    @Override
    protected void fillToolTip(List<Component> list) {

    }
}
