package com.pryzmm.magmamacro.screen.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pryzmm.magmamacro.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class Dropdown extends AbstractWidget {

    private static final ResourceLocation valueTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/dropdown/value.png");
    private static final ResourceLocation openDropdownTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/dropdown/open_dropdown.png");
    private static final ResourceLocation closeDropdownTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/dropdown/close_dropdown.png");
    private static final ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/dropdown/background.png");
    private static final ResourceLocation selectTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/dropdown/select.png");

    private final DropdownOptions options;
    private final String labelKey;
    private final TaskDisplay taskDisplay;
    private String selectedKey;

    public static class DropdownOptions {
        List<String> values;
        Integer selectedIndex;

        DropdownOptions(List<String> values) {
            this.values = values;
            this.selectedIndex = null;
        }
    }

    public Dropdown(int x, int y, DropdownOptions dropdownOptions, String key, TaskDisplay display) {
        super(x, y, 80, 20, Component.empty());
        options = dropdownOptions;
        labelKey = key;
        taskDisplay = display;
        selectedKey = null;
    }

    public DropdownOptions getOptions() {
        return options;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public TaskDisplay getTaskDisplay() {
        return taskDisplay;
    }

    public String getSelectedKey() {
        return this.selectedKey;
    }

    public void setSelectedKey(String selectedKey) {
        this.selectedKey = selectedKey;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, TaskDisplay parent, boolean suppressExpanded) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        guiGraphics.blit(valueTexture, this.getX(), this.getY(), 0, 0, 60, 20, 100, 20);

        if (this.options.selectedIndex != null) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(this.options.values.get(this.options.selectedIndex)), this.getX() + 3, this.getY() + 6, 0xFFFFFFFF);
        }

        if (parent.isActiveDropdown(this)) {
            guiGraphics.blit(closeDropdownTexture, this.getX() + 60, this.getY(), 0, 0, 20, 20, 20, 20);
            if (!suppressExpanded) {
                int height = options.values.size() * 15;
                guiGraphics.blit(backgroundTexture, this.getX(), this.getY() + 20, 0, 0, 80, height);
                guiGraphics.renderOutline(this.getX(), this.getY() + 20, 80, height, 0xFF000000);
                int i = 0;
                for (String value : options.values) {
                    guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(value), this.getX() + 3, this.getY() + 23 + (15 * i), 0xFFFFFFFF);
                    guiGraphics.blit(selectTexture, this.getX() + 64, this.getY() + 21 + (15 * i), 0, 0, 12, 12, 12, 12);
                    if (mouseX >= this.getX() + 64 && mouseX < this.getX() + 76 && mouseY >= this.getY() + 21 + (15 * i) && mouseY < this.getY() + 33 + (15 * i))
                        guiGraphics.renderOutline(this.getX() + 64, this.getY() + 21 + (15 * i), 12, 12, 0xFFFFFFFF);
                    i++;
                }
            }
        } else guiGraphics.blit(openDropdownTexture, this.getX() + 60, this.getY(), 0, 0, 20, 20, 20, 20);

        if (mouseX >= this.getX() + 60 && mouseX < this.getX() + 80 && mouseY >= this.getY() && mouseY < this.getY() + 20)
            guiGraphics.renderOutline(this.getX() + 60, this.getY(), 20, 20, 0xFFFFFFFF);
    }


    public void renderExpandedMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);

        guiGraphics.blit(valueTexture, this.getX(), this.getY(), 0, 0, 60, 20, 100, 20);
        if (this.options.selectedIndex != null) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(this.options.values.get(this.options.selectedIndex)), this.getX() + 3, this.getY() + 6, 0xFFFFFFFF);
        }

        guiGraphics.blit(closeDropdownTexture, this.getX() + 60, this.getY(), 0, 0, 20, 20, 20, 20);
        if (mouseX >= this.getX() + 60 && mouseX < this.getX() + 80 && mouseY >= this.getY() && mouseY < this.getY() + 20) {
            guiGraphics.renderOutline(this.getX() + 60, this.getY(), 20, 20, 0xFFFFFFFF);
        }

        int height = (options.values.size() * 15) + 1;
        guiGraphics.blit(backgroundTexture, this.getX(), this.getY() + 19, 0, 0, 80, height);
        guiGraphics.renderOutline(this.getX(), this.getY() + 19, 80, height, 0xFF000000);

        int i = 0;
        for (String value : options.values) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(value), this.getX() + 3, this.getY() + 23 + (15 * i), 0xFFFFFFFF);
            guiGraphics.blit(selectTexture, this.getX() + 64, this.getY() + 21 + (15 * i), 0, 0, 12, 12, 12, 12);

            if (mouseX >= this.getX() + 64 && mouseX < this.getX() + 76 && mouseY >= this.getY() + 21 + (15 * i) && mouseY < this.getY() + 33 + (15 * i)) {
                guiGraphics.renderOutline(this.getX() + 64, this.getY() + 21 + (15 * i), 12, 12, 0xFFFFFFFF);
            }

            i++;
        }

        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {}

    public void mouseClicked(double mouseX, double mouseY, int button, TaskDisplay parent) {

        boolean clickingToggleButton = mouseX >= this.getX() + 60 && mouseX < this.getX() + 80 && mouseY >= this.getY() && mouseY < this.getY() + 20;

        if (clickingToggleButton) {
            if (parent.activeDropdown != this) parent.toggleDropdown(this);
            else parent.closeDropdown();
            super.mouseClicked(mouseX, mouseY, button);
            return;
        }

        if (parent.activeDropdown == this) {
            int i = 0;
            for (String value : options.values) {
                int selectX = this.getX() + 64;
                int selectY = this.getY() + 21 + (15 * i);
                int selectWidth = 12;
                int selectHeight = 12;

                boolean xMatch = mouseX >= selectX && mouseX < selectX + selectWidth;
                boolean yMatch = mouseY >= selectY && mouseY < selectY + selectHeight;

                if (xMatch && yMatch) {
                    this.options.selectedIndex = this.options.values.indexOf(value);
                    onValueSelected(value);
                    parent.activeDropdown = null;
                    super.mouseClicked(mouseX, mouseY, button);
                    return;
                }
                i++;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    protected void onValueSelected(String value) {
        setSelectedKey(value);
        this.getTaskDisplay().refreshDropdowns();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

}