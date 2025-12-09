package com.pryzmm.magmamacro.screen.util;

import com.pryzmm.magmamacro.data.FileHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ScrollableList extends AbstractSelectionList<ScrollableList.Entry> {

    private final int xPos;

    public ScrollableList(int x, int y, int width, int height, int itemHeight) {
        super(Minecraft.getInstance(), width, height, y, itemHeight);
        this.xPos = x;
        this.setPosition(x, y);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.xPos + this.width - 6;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    public static class Entry extends AbstractSelectionList.Entry<Entry> {
        private final Component text;
        private final Button button;
        private final TaskDisplay taskDisplay;
        private final FileHandler.Macro macro;

        public Entry(Component text) {
            this.text = text;
            this.button = null;
            this.taskDisplay = null;
            this.macro = null;
        }

        public Entry(Button button, @NotNull FileHandler.Macro macro) { // For macro list
            this.text = null;
            this.button = button;
            this.taskDisplay = null;
            this.macro = macro;
        }

        public Entry(TaskDisplay display) {
            this.text = null;
            this.button = null;
            this.taskDisplay = display;
            this.macro = null;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            if (button != null && macro != null) {
                button.setX(x + (width / 4) - 4);
                button.setY(y);
                button.setWidth(width / 2);
                button.setHeight(height);
                button.setMessage(Component.literal(macro.name));
                button.render(graphics, mouseX, mouseY, partialTick);
            } else if (taskDisplay != null) {
                taskDisplay.setY(y);
                taskDisplay.setSuppressExpandedRendering(true);
                taskDisplay.render(graphics, mouseX, mouseY, partialTick);
            } else if (text != null) {
                Minecraft minecraft = Minecraft.getInstance();
                graphics.drawString(minecraft.font, text, x + (width / 4) + (minecraft.font.width(text) / 2) - 12, y, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.button != null) return this.button.mouseClicked(mouseX, mouseY, button);
            if (this.taskDisplay != null) return this.taskDisplay.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean anyDropdownActive = false;
        for (int i = 0; i < this.getItemCount(); i++) {
            Entry entry = this.getEntry(i);
            if (entry.taskDisplay != null && entry.taskDisplay.hasActiveDropdown()) {
                anyDropdownActive = true;
                break;
            }
        }

        for (int i = 0; i < this.getItemCount(); i++) {
            Entry entry = this.getEntry(i);
            if (entry.taskDisplay != null) {
                entry.taskDisplay.setSuppressExpandedRendering(anyDropdownActive);
            }
        }

        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < this.getItemCount(); i++) {
            Entry entry = this.getEntry(i);
            if (entry.taskDisplay != null && entry.taskDisplay.hasActiveDropdown()) {
                entry.taskDisplay.renderExpandedDropdown(graphics, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        for (int i = 0; i < this.getItemCount(); i++) {
            Entry entry = this.getEntry(i);
            if (entry.taskDisplay != null) {
                TaskDisplay td = entry.taskDisplay;
                boolean inTaskDisplay = mouseX >= td.getX() && mouseX <= td.getX() + td.getWidth() && mouseY >= td.getY() && mouseY <= td.getY() + td.getHeight();
                boolean inExpandedDropdown = false;

                if (td.hasActiveDropdown()) {
                    int maxDropdownHeight = 150;
                    inExpandedDropdown = mouseX >= td.getX() && mouseX <= td.getX() + td.getWidth() && mouseY >= td.getY() && mouseY <= td.getY() + td.getHeight() + maxDropdownHeight;
                }

                if (inTaskDisplay || inExpandedDropdown) {
                    if (entry.mouseClicked(mouseX, mouseY, button)) return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderSelection(@NotNull GuiGraphics pGuiGraphics, int pTop, int pWidth, int pHeight, int pOuterColor, int pInnerColor) {}

}