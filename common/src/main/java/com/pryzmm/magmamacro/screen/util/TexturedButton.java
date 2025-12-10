package com.pryzmm.magmamacro.screen.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends Button {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int u;
    private final int v;

    public TexturedButton(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight, int u, int v, Tooltip tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.setTooltip(tooltip);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), this.u, this.v, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFFFFFFFF);
    }
}