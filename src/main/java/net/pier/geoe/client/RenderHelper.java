package net.pier.geoe.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class RenderHelper extends RenderType {


    private static RenderType.CompositeState translucentState(RenderStateShard.ShaderStateShard pState) {
        return RenderType.CompositeState.builder().setCullState(RenderStateShard.NO_CULL).setLightmapState(LIGHTMAP).setDepthTestState(RenderStateShard.NO_DEPTH_TEST).setOutputState(RenderStateShard.OUTLINE_TARGET).setShaderState(pState).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
    }
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    private static final RenderType DEPTH_TRANSLUCENT = RenderType.create("depth_translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, translucentState(RENDERTYPE_TRANSLUCENT_SHADER));

    private static final Function<ResourceLocation, RenderType> GUI_TRANSLUCENT = Util.memoize(texture ->
            RenderType.create("transparent_gui_" + texture.toString(),DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setTextureState(new TextureStateShard(texture, false, false))
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(false)));

    public static RenderType depthTranslucent() {
        return DEPTH_TRANSLUCENT;
    }

    public static RenderType getGuiTranslucent(ResourceLocation texture)
    {
        return GUI_TRANSLUCENT.apply(texture);
    }

    public RenderHelper(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }


}
