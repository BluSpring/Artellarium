package xyz.bluspring.worldcorruption.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import xyz.bluspring.worldcorruption.block.entity.ArtellicCrystalBlockEntity

@Environment(EnvType.CLIENT)
class ArtellicCrystalRenderer(private val ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<ArtellicCrystalBlockEntity> {
    private val mc = Minecraft.getInstance()

    override fun render(
        blockEntity: ArtellicCrystalBlockEntity, delta: Float,
        poseStack: PoseStack, multiBufferSource: MultiBufferSource,
        light: Int, overlay: Int
    ) {
        poseStack.pushPose()

        val offset = Mth.sin((blockEntity.level!!.gameTime + delta) / 8f) / 4.0
        poseStack.translate(.0, offset, .0)

        val model = mc.modelManager.getModel(CRYSTAL_MODEL_LOCATION)

        mc.blockRenderer.modelRenderer.renderModel(
            poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), blockEntity.blockState,
            model,
            1f, 1f, 1f,
            16777215, overlay
        )

        poseStack.popPose()
    }

    companion object {
        val CRYSTAL_MODEL_LOCATION = ModelResourceLocation("artellarium:artellic_crystal_crystal")
    }
}