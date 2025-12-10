package com.pryzmm.magmamacro.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pryzmm.magmamacro.Constants;
import com.pryzmm.magmamacro.data.CachedMacros;
import com.pryzmm.magmamacro.data.FileHandler;
import com.pryzmm.magmamacro.keybinds.MacroRunner;
import com.pryzmm.magmamacro.screen.creation.MacroCreation;
import com.pryzmm.magmamacro.screen.util.ScrollableList;
import com.pryzmm.magmamacro.screen.util.TaskDisplay;
import com.pryzmm.magmamacro.screen.util.TexturedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MacroScreen extends Screen {

    private static final float SCROLL_SPEED = 0.5f;
    private static final float ROTATION_ANGLE = 15f;
    private static final int TEXTURE_SIZE = 64;
    private static final float OPACITY = 0.1f;

    public static boolean choosingKeybind = false;
    public static boolean inputtingName = false;
    public static boolean inputtingDelay = false;
    public static boolean inputtingLength = false;
    public static TaskDisplay lastFocusedTaskDisplay = null;

    private static final ResourceLocation newMacroTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/new_macro.png");
    private static final ResourceLocation newTaskTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/new_task.png");
    private static final ResourceLocation changeNameTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/change_name.png");
    private static final ResourceLocation bindMacroTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/bind_macro.png");
    private static final ResourceLocation deleteMacroTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/delete_macro.png");
    private static final ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/macro_background.png");

    public static AbstractSelectionList<ScrollableList.Entry> macroList = null;
    public static AbstractSelectionList<ScrollableList.Entry> taskList = null;
    public static FileHandler.Macro selectedMacro = null;

    private EditBox inputBox;
    public static EditBox numberInputBox;
    private float scrollOffset = 0f;

    // Store references to conditional buttons
    private TexturedButton newTaskButton;
    private TexturedButton changeNameButton;
    private TexturedButton bindMacroButton;
    private TexturedButton deleteMacroButton;

    public MacroScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget( // Close Button
            Button.builder(Component.translatable("screen.magmamacro.close"), button -> this.onClose())
                .bounds(this.width - 105, this.height - 25, 100, 20)
                .build()
        );

        this.addRenderableWidget( // New Macro Button
            new TexturedButton(
                5, 5, 20, 20,
                newMacroTexture, 20, 20,
                0, 0,
                Tooltip.create(Component.translatable("button.magmamacro.tooltip.create_macro")),
                button -> MacroCreation.createNewMacro()
            )
        );

        macroList = this.addRenderableWidget( // Macro List
            new ScrollableList(
                this.width / 12, this.height / 12,
                this.width / 5, (this.height / 12) * 10,
                20
            )
        );
        macroList.children().add(new ScrollableList.Entry(Component.translatable("screen.magmamacro.your_macros")));
        for (FileHandler.Macro macro : CachedMacros.cachedMacros) {
            macroList.children().add(new ScrollableList.Entry(Button.builder(Component.literal(macro.name), button -> MacroCreation.openMacro(macro)).build(), macro));
        }

        taskList = this.addRenderableWidget( // Task list
            new ScrollableList(
                (int) (((float) this.width / 12) * 3.5f), (this.height / 12) * 2,
                (int) (this.width / 1.6f), (this.height / 12) * 9,
                30
            )
        );


        newTaskButton = new TexturedButton( // New Task Button
            (int) (((float) this.width / 12) * 3.5f), (this.height / 12),
            20, 20,
            newTaskTexture, 20, 20,
            0, 0,
            Tooltip.create(Component.translatable("button.magmamacro.tooltip.new_task")),
            button -> {
                Integer actionId = selectedMacro.actions.keySet().stream()
                        .max(Integer::compareTo)
                        .map(maxId -> maxId + 1)
                        .orElse(1);
                taskList.children().add(new ScrollableList.Entry(new TaskDisplay(taskList.getX() + 5, taskList.getWidth() - 16, 28, actionId, null, null, null, null, null, selectedMacro)));
            }
        );
        newTaskButton.visible = selectedMacro != null;
        this.addRenderableWidget(newTaskButton);

        changeNameButton = new TexturedButton( // Change Macro Name Button
            (int) (((float) this.width / 12) * 4f), (this.height / 12),
            20, 20,
            changeNameTexture, 20, 20,
            0, 0,
            Tooltip.create(Component.translatable("button.magmamacro.tooltip.rename_macro")),
            button -> {
                inputtingName = true;
                if (inputBox != null) {
                    inputBox.setFocused(true);
                    inputBox.setValue("");
                }
            }
        );
        changeNameButton.visible = selectedMacro != null;
        this.addRenderableWidget(changeNameButton);

        bindMacroButton = new TexturedButton( // Bind Macro Button
            (int) (((float) this.width / 12) * 4.5f), (this.height / 12),
            20, 20,
            bindMacroTexture, 20, 20,
            0, 0,
            Tooltip.create(Component.translatable("button.magmamacro.tooltip.bind_macro")),
            button -> choosingKeybind = true
        );
        bindMacroButton.visible = selectedMacro != null;
        this.addRenderableWidget(bindMacroButton);

        deleteMacroButton = new TexturedButton( // Delete Macro Button
            (int) ((((float) this.width / 12) * 3.5f) + (this.width / 1.6f)) - 20, (this.height / 12),
            20, 20,
            deleteMacroTexture, 20, 20,
            0, 0,
            Tooltip.create(Component.translatable("button.magmamacro.tooltip.delete_macro")),
            button -> {
                // Remove registered bind if the macro had one
                if (selectedMacro != null && selectedMacro.bind != null) {
                    MacroRunner.removeBindFromMacro(selectedMacro.id, selectedMacro.bind);
                }

                // Remove macro from file and clear UI selection/state
                FileHandler.MacroFile file = FileHandler.file;
                if (selectedMacro != null) {
                    file.macros.remove(selectedMacro.id);
                }
                taskList.children().clear();
                selectedMacro = null;

                // Refresh cached macros and rebuild macro list
                CachedMacros.refreshCache(file);
                macroList.children().clear();
                macroList.children().add(new ScrollableList.Entry(Component.translatable("screen.magmamacro.your_macros")));
                for (FileHandler.Macro macro : CachedMacros.cachedMacros) {
                    macroList.children().add(new ScrollableList.Entry(Button.builder(Component.literal(macro.name), b -> MacroCreation.openMacro(macro)).build(), macro));
                }

                // Persist changes
                FileHandler.saveMacroFile(file);
            }
        );
        deleteMacroButton.visible = selectedMacro != null;
        this.addRenderableWidget(deleteMacroButton);

        inputBox = new EditBox(this.font, (this.width / 2) - 100, (this.height / 2) + 10, 200, 20, Component.empty());
        inputBox.setMaxLength(255);

        numberInputBox = new NumericEditBox(this.font, (this.width / 2) - 20, (this.height / 2) + 10, 40, 20, Component.empty());
        numberInputBox.setMaxLength(5);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (choosingKeybind) {
            choosingKeybind = false;
            String keyName = InputConstants.getKey(keyCode, scanCode).getName();
            if (InputConstants.getKey(keyCode, scanCode).getValue() != InputConstants.KEY_ESCAPE) {
                selectedMacro.bind = keyName;
                MacroRunner.addBindToMacro(selectedMacro.id, keyName);
                FileHandler.updateMacroInFile(selectedMacro);
            } else {
                selectedMacro.bind = null;
                MacroRunner.removeBindFromMacro(selectedMacro.id, keyName);
                FileHandler.updateMacroInFile(selectedMacro);
            }
            return true;
        }
        if (inputtingName) {
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_ESCAPE) {
                inputtingName = false;
                inputBox.setFocused(false);
                return true;
            }
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_RETURN) {
                selectedMacro.name = inputBox.getValue();
                inputtingName = false;
                inputBox.setFocused(false);
                FileHandler.updateMacroInFile(selectedMacro);
                return true;
            }
            return inputBox.keyPressed(keyCode, scanCode, modifiers);
        }
        if (inputtingDelay) {
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_ESCAPE) {
                inputtingDelay = false;
                numberInputBox.setFocused(false);
                return true;
            }
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_RETURN) {
                selectedMacro.actions.get(lastFocusedTaskDisplay.getCurrentActionID()).delay(Integer.valueOf(numberInputBox.getValue()));
                inputtingDelay = false;
                numberInputBox.setFocused(false);
                FileHandler.updateMacroInFile(selectedMacro);
                return true;
            }
            if (numberInputBox != null) {
                return numberInputBox.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        if (inputtingLength) {
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_ESCAPE) {
                inputtingLength = false;
                numberInputBox.setFocused(false);
                return true;
            }
            if (InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_RETURN) {
                selectedMacro.actions.get(lastFocusedTaskDisplay.getCurrentActionID()).length(Integer.valueOf(numberInputBox.getValue()));
                inputtingLength = false;
                numberInputBox.setFocused(false);
                FileHandler.updateMacroInFile(selectedMacro);
                return true;
            }
            if (numberInputBox != null) {
                return numberInputBox.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        TaskDisplay focused = TaskDisplay.getFocusedTaskDisplay();
        if (focused != null && focused.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (inputtingName && inputBox != null) return inputBox.charTyped(codePoint, modifiers);
        if (inputtingDelay && numberInputBox != null) return numberInputBox.charTyped(codePoint, modifiers);
        if (inputtingLength && numberInputBox != null) return numberInputBox.charTyped(codePoint, modifiers);

        if (!choosingKeybind && !inputtingName && !inputtingDelay && !inputtingLength) {
            TaskDisplay focused = TaskDisplay.getFocusedTaskDisplay();
            if (focused != null && focused.charTyped(codePoint, modifiers)) return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (choosingKeybind) return true;
        if (inputtingName && inputBox != null) return inputBox.mouseClicked(mouseX, mouseY, button);
        if (inputtingDelay && numberInputBox != null) return numberInputBox.mouseClicked(mouseX, mouseY, button);
        if (inputtingLength && numberInputBox != null) return numberInputBox.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (choosingKeybind || inputtingName || inputtingDelay || inputtingLength) return;
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        boolean hasMacroSelected = selectedMacro != null;
        if (newTaskButton != null) newTaskButton.visible = hasMacroSelected;
        if (changeNameButton != null) changeNameButton.visible = hasMacroSelected;
        if (deleteMacroButton != null) deleteMacroButton.visible = hasMacroSelected;
        if (bindMacroButton != null) {
            bindMacroButton.visible = hasMacroSelected;
            if (bindMacroButton.visible) {
                if (selectedMacro.bind != null) bindMacroButton.setTooltip(Tooltip.create(Component.translatable("button.magmamacro.tooltip.bound", InputConstants.getKey(selectedMacro.bind).getDisplayName())));
                else bindMacroButton.setTooltip(Tooltip.create(Component.translatable("button.magmamacro.tooltip.bind_macro")));
            }
        }

        renderAnimatedBackground(guiGraphics);

        if (choosingKeybind || inputtingName || inputtingDelay || inputtingLength) super.render(guiGraphics, -1, -1, partialTick);
        else super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (minecraft != null && minecraft.screen != null) {
            if (choosingKeybind) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0f, 0.0f, 400.0f);
                guiGraphics.fill(0, 0, minecraft.screen.width, minecraft.screen.height, 0xDD000000);
                guiGraphics.drawString(minecraft.font, Component.translatable("screen.magmamacro.choose_bind"), (minecraft.screen.width / 2) - (minecraft.font.width(Component.translatable("screen.magmamacro.choose_bind").getString()) / 2), (minecraft.screen.height / 2) - 5, 0xFFFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.translatable("screen.magmamacro.choose_bind_delete"), (minecraft.screen.width / 2) - (minecraft.font.width(Component.translatable("screen.magmamacro.choose_bind_delete").getString()) / 2), (minecraft.screen.height / 2) + 5, 0xFFBBBBBB);
                guiGraphics.pose().popPose();
            }
            else if (inputtingName && inputBox != null) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0f, 0.0f, 400.0f);
                guiGraphics.fill(0, 0, minecraft.screen.width, minecraft.screen.height, 0xDD000000);
                guiGraphics.drawString(minecraft.font, Component.translatable("screen.magmamacro.input_value"), (minecraft.screen.width / 2) - (minecraft.font.width(Component.translatable("screen.magmamacro.input_value").getString()) / 2), (minecraft.screen.height / 2) - 5, 0xFFFFFFFF);
                inputBox.render(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.pose().popPose();
            }
            else if ((inputtingDelay || inputtingLength) && inputBox != null) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0f, 0.0f, 400.0f);
                guiGraphics.fill(0, 0, minecraft.screen.width, minecraft.screen.height, 0xDD000000);
                guiGraphics.drawString(minecraft.font, Component.translatable("screen.magmamacro.input_number"), (minecraft.screen.width / 2) - (minecraft.font.width(Component.translatable("screen.magmamacro.input_number").getString()) / 2), (minecraft.screen.height / 2) - 5, 0xFFFFFFFF);
                numberInputBox.render(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.pose().popPose();
            }
        }

        if (selectedMacro != null && minecraft != null) {
            guiGraphics.drawString(
                minecraft.font,
                Component.translatable("screen.magmamacro.editing_macro", selectedMacro.name),
                (int) (((float) this.width / 12) * 7.75f) - (minecraft.font.width(Component.translatable("screen.magmamacro.editing_macro", selectedMacro.name)) / 2),
                (int) (((double) this.height / 12) * 1.225f),
                0xFFFFFFFF
            );
        }

    }

    private void renderAnimatedBackground(GuiGraphics guiGraphics) {

        RenderSystem.setShaderTexture(0, backgroundTexture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, OPACITY);
        RenderSystem.enableBlend();

        scrollOffset += SCROLL_SPEED;
        if (scrollOffset >= TEXTURE_SIZE * 1.414f) scrollOffset = scrollOffset % TEXTURE_SIZE;

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(this.width / 2.0, this.height / 2.0, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(ROTATION_ANGLE));
        pose.translate(-this.width / 2.0, -this.height / 2.0, 0);

        int tilesX = (int) Math.ceil(this.width * 1.5 / TEXTURE_SIZE) + 2;
        int tilesY = (int) Math.ceil(this.height * 1.5 / TEXTURE_SIZE) + 2;
        int startX = (int) (-this.width * 0.25 - scrollOffset * 1);
        int startY = (int) (-this.height * 0.25 - scrollOffset * 0);

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                guiGraphics.blit(
                        backgroundTexture,
                        startX + x * TEXTURE_SIZE,
                        startY + y * TEXTURE_SIZE,
                        0, 0,
                        TEXTURE_SIZE, TEXTURE_SIZE,
                        TEXTURE_SIZE, TEXTURE_SIZE
                );
            }
        }

        pose.popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

    }

    @Override
    public void onClose() {
        FileHandler.updateMacroInFile(selectedMacro);
        super.onClose();
        selectedMacro = null;
    }

    private static class NumericEditBox extends EditBox {
        public NumericEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            if (Character.isDigit(codePoint)) {
                return super.charTyped(codePoint, modifiers);
            }
            return false;
        }

        @Override
        public void setValue(String value) {
            String filtered = value.replaceAll("[^0-9]", "");
            super.setValue(filtered);
        }
    }

}
