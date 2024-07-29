package net.pier.geoe.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class VertexWrapper implements VertexConsumer {

    private VertexConsumer wrapper;
    private final float alpha;

    public VertexWrapper(VertexConsumer wrapper, float alpha) {
        this.wrapper = wrapper;
        this.alpha = alpha;
    }

    public void setWrapper(VertexConsumer wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public VertexConsumer vertex(double pX, double pY, double pZ) {
        return this.wrapper.vertex(pX,pY,pZ);
    }

    @Override
    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return this.wrapper.color(pRed,pGreen,pBlue,(int)(this.alpha  * pAlpha));
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        return this.wrapper.uv(pU,pV);
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return this.wrapper.overlayCoords(pU,pV);
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        return this.wrapper.uv2(pU,pV);
    }

    @Override
    public VertexConsumer normal(float pX, float pY, float pZ) {
        return this.wrapper.normal(pX,pY,pZ);
    }

    @Override
    public void endVertex() {
        this.wrapper.endVertex();
    }

    @Override
    public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
        this.wrapper.defaultColor(pDefaultR,pDefaultG,pDefaultB,pDefaultA);
    }

    @Override
    public void unsetDefaultColor() {
        this.wrapper.unsetDefaultColor();
    }

    public VertexConsumer getWrapper() {
        return wrapper;
    }
}
