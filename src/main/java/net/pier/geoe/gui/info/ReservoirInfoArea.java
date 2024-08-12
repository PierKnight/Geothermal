package net.pier.geoe.gui.info;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.fluids.FluidStack;
import net.pier.geoe.capability.reservoir.Reservoir;
import net.pier.geoe.client.RenderHelper;
import net.pier.geoe.util.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static net.pier.geoe.client.GuiHelper.drawTexturedColoredRect;
import static net.pier.geoe.client.GuiHelper.getSprite;

public class ReservoirInfoArea extends InfoArea{

    private final Supplier<Reservoir> reservoir;
    private final NormalNoise noise;

    private final int tileSize = 5;
    private final float a;
    private final float b;
    private final float noiseScale;
    private final boolean[][] isCave;

    private int holeSurface = 0;

    public ReservoirInfoArea(Rect2i area, Supplier<Reservoir> reservoir, long seed) {
        super(area);
        this.reservoir = reservoir;
        RandomSource randomSource = new XoroshiroRandomSource(seed);
        this.noise = NormalNoise.create(randomSource,new NormalNoise.NoiseParameters(-1,1.0));
        this.a = area.getWidth() * 0.5F;
        this.b = area.getWidth() * 0.5F;
        this.noiseScale = 0.5F;

        this.isCave = new boolean[area.getWidth()][area.getHeight()];

        for(int ww = 0;ww < this.area.getWidth();ww++)
            for (int hh = 0; hh < this.area.getHeight(); hh++) {
                this.isCave[ww][hh] = this.isInside(ww - (int) (this.area.getWidth() * 0.5), hh - (int) (this.area.getHeight() * 0.5));
                if(this.isCave[ww][hh]) holeSurface += 1;
            }
    }

    private double getPerturbedRadius(double theta) {
        double rOriginal = (a * b) / Math.sqrt(b * b * Math.cos(theta) * Math.cos(theta) + a * a * Math.sin(theta) * Math.sin(theta));
        double noiseValue = this.noise.getValue(Math.cos(theta), Math.sin(theta),0); // Get noise value for the angle
        return rOriginal * (1 - (noiseValue + 1) * 0.5 * noiseScale);
    }

    // Function to check if point (x, y) is inside the perturbed ellipse
    private boolean isInside(double x, double y) {
        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);
        double rPerturbed = getPerturbedRadius(theta);
        return r <= rPerturbed;
    }

    @Override
    public void draw(PoseStack transform, List<Component> tooltip, int mouseX, int mouseY) {

        Reservoir reservoir = this.reservoir.get();

        if(reservoir == null)
            return;

        FluidStack[] fluids = new FluidStack[]{reservoir.getFluid(),reservoir.getInput()};
        Float[] fluidTotalArea = Arrays.stream(fluids).map(fluidStack -> {
            float r = (float) fluidStack.getAmount() / reservoir.getCapacity();
            return this.holeSurface * r;
        }).toArray(Float[]::new);
        int areaX = this.area.getX();
        int areaY = this.area.getY();

        TextureAtlasSprite stoneSprite = getSprite(new ResourceLocation("minecraft","block/stone"));

        transform.pushPose();
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        RenderType renderType = RenderHelper.getGuiTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer builder = buffer.getBuffer(renderType);

        float[] fluidHoleAMount = new float[fluidTotalArea.length];
        int currentFluid = 0;


        for(int hh = this.area.getHeight() - 1;hh >= 0;hh--)
        {
            float totalCaveHoles = 0;
            Arrays.fill(fluidHoleAMount, 0);

            for(int ww = 0;ww < this.area.getWidth() && currentFluid < fluidTotalArea.length;ww++)
            {
                if(this.isCave[ww][hh])
                {
                    float fluidAmount = Math.min(fluidTotalArea[currentFluid], 1.0F);
                    fluidHoleAMount[currentFluid] += fluidAmount;
                    totalCaveHoles += fluidAmount;
                    fluidTotalArea[currentFluid] = Math.max(fluidTotalArea[currentFluid] - 1.0F, 0.0F);
                    if(fluidTotalArea[currentFluid] == 0.0F)
                        currentFluid++;
                }
            }
            for(int ww = 0;ww < this.area.getWidth();ww++)
            {
                boolean isTunnel = ww == (int)(this.area.getWidth() * 0.5) && hh <= (int)(this.area.getHeight() * 0.5);
                float backgroundColor = this.isCave[ww][hh] || isTunnel ? 0.4F : 0.7F;

                //draw the darkened background
                drawTexturedColoredRect(builder, transform, areaX+ww*tileSize, areaY+hh*tileSize, tileSize, tileSize,
                        backgroundColor,backgroundColor,backgroundColor,
                        1, stoneSprite.getU0(), stoneSprite.getU1(), stoneSprite.getV0(), stoneSprite.getV1());

                if(this.isCave[ww][hh]) {
                    //draws the fluids in this row using fluidHoleAMount
                    float fluidHeight = 0;
                    if(totalCaveHoles > 0)
                        for (int i = fluidHoleAMount.length - 1; i >= 0; i--) {
                            int col = fluids[i].getFluid().getAttributes().getColor(fluids[i]);
                            float ratio = fluidHoleAMount[i] / totalCaveHoles;
                            TextureAtlasSprite fluidSprite = getSprite(fluids[i].getFluid().getAttributes().getStillTexture());

                            if(isInArea(areaX + ww * tileSize, areaY + hh * tileSize + fluidHeight, tileSize, tileSize * ratio, mouseX, mouseY))
                                TextUtils.addFluidStackTooltip(fluids[i], tooltip, reservoir.getCapacity());

                            drawTexturedColoredRect(builder, transform, areaX + ww * tileSize, areaY + hh * tileSize + fluidHeight, tileSize, tileSize * ratio,
                                    (col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f,
                                    1F, fluidSprite.getU0(), fluidSprite.getU1(), fluidSprite.getV0(), fluidSprite.getV0() + (fluidSprite.getV1() - fluidSprite.getV0()) * ratio);
                            fluidHeight += ratio * tileSize;
                        }
                }
            }
        }

        buffer.endBatch();
        transform.popPose();
    }

    private boolean isInArea(float x, float y, float width, float height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
