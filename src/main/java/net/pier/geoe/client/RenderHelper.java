package net.pier.geoe.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderHelper extends RenderType {


    private static RenderType.CompositeState translucentState(RenderStateShard.ShaderStateShard pState) {
        return RenderType.CompositeState.builder().setCullState(RenderStateShard.NO_CULL).setLightmapState(LIGHTMAP).setDepthTestState(RenderStateShard.NO_DEPTH_TEST).setOutputState(RenderStateShard.OUTLINE_TARGET).setShaderState(pState).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
    }
    private static final RenderType DEPTH_TRANSLUCENT = RenderType.create("depth_translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, translucentState(RENDERTYPE_TRANSLUCENT_SHADER));

    public static RenderType depthTranslucent() {
        return DEPTH_TRANSLUCENT;
    }


    public RenderHelper(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
}
