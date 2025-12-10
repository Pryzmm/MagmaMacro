package com.pryzmm.magmamacro.screen.util;

import com.pryzmm.magmamacro.Constants;
import com.pryzmm.magmamacro.data.CachedMacros;
import com.pryzmm.magmamacro.data.FileHandler;
import com.pryzmm.magmamacro.screen.MacroScreen;
import com.pryzmm.magmamacro.screen.creation.MacroCreation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class TaskDisplay extends AbstractWidget {

    private static class DropdownEntry {
        String labelKey;
        Dropdown dropdown;
        int spacing;

        DropdownEntry(String labelKey, Dropdown dropdown, int spacing) {
            this.labelKey = labelKey;
            this.dropdown = dropdown;
            this.spacing = spacing;
        }
    }

    private final List<DropdownEntry> dropdowns = new ArrayList<>();
    public Dropdown activeDropdown;
    private static TaskDisplay lastActiveTaskDisplay = null;
    private boolean suppressExpandedRendering = false;
    public static final List<TaskDisplay> allTaskDisplays = new ArrayList<>();

    FileHandler.Macro currentMacro;
    private final Integer actionID;
    public Integer getCurrentActionID() {
        return this.actionID;
    }

    private final Map<String, DropdownEntry> dropdownEntries = new HashMap<>();

    private static final ResourceLocation delayTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/delay_button.png");
    private static final ResourceLocation deleteTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/delete_macro.png");
    private static final ResourceLocation lengthTexture = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/macro/length_button.png");

    private TexturedButton delayButton;
    private TexturedButton deleteButton;

    private final EditBox inputBox = new EditBox(Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());

    private static TaskDisplay focusedTaskDisplay = null;
    public static TaskDisplay getFocusedTaskDisplay() {
        return focusedTaskDisplay;
    }
    private static void setFocusedTaskDisplay(TaskDisplay td) {
        for (TaskDisplay t : allTaskDisplays) t.inputBox.setFocused(t == td);
        focusedTaskDisplay = td;
    }

    public TaskDisplay(int x, int width, int height, Integer actionID, String action, String value, String direction, Integer length, Integer delay, FileHandler.Macro macro) {
        super(x, 0, width, height, Component.empty());
        currentMacro = macro;
        this.actionID = actionID;
        DropdownEntry entry = addDropdown("screen.magmamacro.action", new Dropdown.DropdownOptions(List.of("option.magmamacro.command", "option.magmamacro.chat", "option.magmamacro.movement")));
        if (action != null && entry.dropdown.getOptions().values.contains("option.magmamacro." + action.toLowerCase())) {
            entry.dropdown.setSelectedKey("screen.magmamacro." + action.toLowerCase());
            entry.dropdown.getOptions().selectedIndex = entry.dropdown.getOptions().values.indexOf("option.magmamacro." + action.toLowerCase());
            if (action.contains("MOVEMENT")) {
                DropdownEntry dirEntry = addDropdown("screen.magmamacro.direction", new Dropdown.DropdownOptions(List.of("option.magmamacro.backwards", "option.magmamacro.forwards", "option.magmamacro.left", "option.magmamacro.right")));
                if (direction != null) {
                    String directionKey = "option.magmamacro." + direction.toLowerCase();
                    int index = dirEntry.dropdown.getOptions().values.indexOf(directionKey);
                    if (index != -1) {
                        dirEntry.dropdown.setSelectedKey(directionKey);
                        dirEntry.dropdown.getOptions().selectedIndex = index;
                    }
                }
            } else if (action.contains("CHAT") || action.contains("COMMAND")) {
                inputBox.visible = true;
                inputBox.active = true;
                inputBox.setPosition(this.getX() + 4, this.getY() + 4);
                if (currentMacro.actions.get(this.actionID).value() != null) inputBox.insertText(currentMacro.actions.get(this.actionID).value());
            }
        }
        refreshDropdowns();
        allTaskDisplays.add(this);
        initButtons();
    }

    private void initButtons() {
        delayButton = new TexturedButton(
            0, 0, 12, 12,
            delayTexture, 12, 12,
            0, 0,
            Tooltip.create(Component.empty()),
            button -> {
                MacroScreen.lastFocusedTaskDisplay = this;
                MacroScreen.inputtingDelay = true;
                if (MacroScreen.numberInputBox != null) {
                    MacroScreen.numberInputBox.setFocused(true);
                    MacroScreen.numberInputBox.setValue("");
                }
            }
        );

        deleteButton = new TexturedButton(
            0, 0, 12, 12,
            deleteTexture, 12, 12,
            0, 0,
            Tooltip.create(Component.translatable("button.magmamacro.tooltip.delete_task")),
            button -> {
                if (MacroScreen.lastFocusedTaskDisplay == this) MacroScreen.lastFocusedTaskDisplay = null;
                currentMacro.actions.remove(this.actionID);
                FileHandler.updateMacroInFile(currentMacro);
                MacroCreation.openMacro(currentMacro);
            }
        );

        inputBox.setMaxLength(255);
        inputBox.visible = false;
        inputBox.active = false;
    }

    public void refreshDropdowns() {
        Dropdown actionDropdown = null;
        Dropdown directionDropdown = null;
        List<DropdownEntry> currentEntries = new ArrayList<>(dropdowns);
        for (DropdownEntry entry : currentEntries) {
            if (entry.dropdown.getLabelKey().equals("screen.magmamacro.action")) {
                actionDropdown = entry.dropdown;
            }
            if (entry.dropdown.getLabelKey().equals("screen.magmamacro.direction")) {
                directionDropdown = entry.dropdown;
            }
        }

        Set<String> desiredLabels = new HashSet<>();
        desiredLabels.add("screen.magmamacro.action");
        if (actionDropdown != null && actionDropdown.getSelectedKey() != null && actionDropdown.getSelectedKey().contains(".movement")) {
            desiredLabels.add("screen.magmamacro.direction");
        }

        for (DropdownEntry entry : currentEntries) {
            if (!desiredLabels.contains(entry.labelKey)) removeDropdown(entry.labelKey);
        }

        if (actionDropdown != null && actionDropdown.getSelectedKey() != null) {
            FileHandler.MacroAction currentAction = currentMacro.actions.get(this.actionID);
            if (currentAction == null) currentAction = new FileHandler.MacroAction(null, null, null, null, 0);

            currentAction.action(actionDropdown.getSelectedKey().split("magmamacro\\.")[1].toUpperCase());

            if (actionDropdown.getSelectedKey().contains(".movement")) {
                String dirLabel = "screen.magmamacro.direction";
                String dirTranslated = Component.translatable(dirLabel).getString();
                if (!dropdownEntries.containsKey(dirTranslated)) {
                    addDropdown(dirLabel, new Dropdown.DropdownOptions(List.of("option.magmamacro.backwards", "option.magmamacro.forwards", "option.magmamacro.left", "option.magmamacro.right")));
                }

                if (directionDropdown != null && directionDropdown.getSelectedKey() != null) {
                    String direction = directionDropdown.getSelectedKey().split("magmamacro\\.")[1].toUpperCase();
                    currentAction.direction(direction);
                    currentAction.length(20);
                }

                inputBox.visible = false;
                inputBox.active = false;
                inputBox.setFocused(false);
            } else if (actionDropdown.getSelectedKey().contains(".command") || actionDropdown.getSelectedKey().contains(".chat")) {
                inputBox.visible = true;
                inputBox.active = true;
            } else {
                inputBox.visible = false;
                inputBox.active = false;
                inputBox.setFocused(false);
            }

            // Update the action in the macro
            this.currentMacro.actions.put(this.actionID, currentAction);
        } else {
            inputBox.visible = false;
            inputBox.active = false;
            inputBox.setFocused(false);
        }
        FileHandler.updateMacroInFile(this.currentMacro);
        CachedMacros.refreshCache(FileHandler.file);
    }

    public boolean isActiveDropdown(Dropdown dropdown) {
        return this.activeDropdown == dropdown;
    }

    public void toggleDropdown(Dropdown dropdown) {
        if (lastActiveTaskDisplay != null && lastActiveTaskDisplay != this) lastActiveTaskDisplay.closeDropdown();
        if (this.activeDropdown == dropdown) {
            this.activeDropdown = null;
            lastActiveTaskDisplay = null;
        } else {
            this.activeDropdown = dropdown;
            lastActiveTaskDisplay = this;
        }
    }

    public void closeDropdown() {
        this.activeDropdown = null;
    }

    public boolean hasActiveDropdown() {
        return this.activeDropdown != null;
    }

    private DropdownEntry addDropdown(String labelKey, Dropdown.DropdownOptions options) {
        DropdownEntry entry = new DropdownEntry(labelKey, new Dropdown(0, 0, options, labelKey, this), 90);
        dropdowns.add(entry);
        dropdownEntries.put(Component.translatable(labelKey).getString(), entry);
        return entry;
    }

    private void removeDropdown(String labelKey) {
        String translatedKey = Component.translatable(labelKey).getString();
        DropdownEntry entryToRemove = dropdownEntries.get(translatedKey);
        if (entryToRemove != null) {
            dropdowns.remove(entryToRemove);
            dropdownEntries.remove(translatedKey);
            if (activeDropdown == entryToRemove.dropdown) {
                activeDropdown = null;
                if (lastActiveTaskDisplay == this) lastActiveTaskDisplay = null;
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        // For the semi-transparent background, the alpha is already in the color value (0x77000000)
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x77000000);

        int offsetX = this.getX() + 4;
        DropdownEntry actionEntry = null;
        for (DropdownEntry entry : dropdowns) {
            if (entry.labelKey.contains(".action")) actionEntry = entry;

            if (entry.dropdown.getSelectedKey() == null) {
                int labelWidth = minecraft.font.width(Component.translatable(entry.labelKey));
                guiGraphics.drawString(minecraft.font, Component.translatable(entry.labelKey), offsetX, this.getY() + 10, 0xFFFFFFFF);
                offsetX += labelWidth + 4;
            }
            entry.dropdown.setPosition(offsetX, this.getY() + 4);
            entry.dropdown.renderWidget(guiGraphics, mouseX, mouseY, this, suppressExpandedRendering);
            offsetX += entry.spacing;
        }

        if (actionEntry != null && (actionEntry.dropdown.getSelectedKey() != null)) {
            if (actionEntry.dropdown.getSelectedKey().contains(".command") || actionEntry.dropdown.getSelectedKey().contains(".chat")) {
                inputBox.setPosition(actionEntry.dropdown.getX() + 90, this.getY() + 4);
                inputBox.active = true;
                inputBox.visible = true;
                inputBox.render(guiGraphics, mouseX, mouseY, partialTick);
            } else {
                inputBox.visible = false;
                inputBox.active = false;
            }
        } else {
            inputBox.visible = false;
            inputBox.active = false;
        }

        if (currentMacro.actions.get(this.actionID) != null) {
            Integer delay = currentMacro.actions.get(this.actionID).delay();
            delayButton.setTooltip(Tooltip.create(Component.translatable("button.magmamacro.tooltip.delay", delay != null ? delay : 0)));
        }
        delayButton.setPosition(this.getX() + this.getWidth() - 13, this.getY() + 1);
        delayButton.render(guiGraphics, mouseX, mouseY, partialTick);

        deleteButton.setPosition(this.getX() + this.getWidth() - 13, this.getY() + 15);
        deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderExpandedDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (activeDropdown != null) activeDropdown.renderExpandedMenu(guiGraphics, mouseX, mouseY);
    }

    public void setSuppressExpandedRendering(boolean suppress) {
        this.suppressExpandedRendering = suppress;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (delayButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteButton.mouseClicked(mouseX, mouseY, button)) return true;

        if (inputBox.visible) {
            if (inputBox.mouseClicked(mouseX, mouseY, button)) {
                setFocusedTaskDisplay(this);
                return true;
            } else {
                if (focusedTaskDisplay == this) setFocusedTaskDisplay(null);
            }
        }

        for (DropdownEntry entry : dropdowns) {
            boolean withinDropdownBounds = (mouseX >= entry.dropdown.getX() && mouseX <= entry.dropdown.getX() + 80 && mouseY >= entry.dropdown.getY() && mouseY <= entry.dropdown.getY() + 20);
            boolean withinMenuBounds = false;
            if (activeDropdown == entry.dropdown) {
                int menuHeight = entry.dropdown.getOptions().values.size() * 15;
                withinMenuBounds = (mouseX >= entry.dropdown.getX() &&
                        mouseX <= entry.dropdown.getX() + 80 &&
                        mouseY >= entry.dropdown.getY() + 20 &&
                        mouseY <= entry.dropdown.getY() + 20 + menuHeight);
            }
            if (withinDropdownBounds || withinMenuBounds) {
                entry.dropdown.mouseClicked(mouseX, mouseY, button, this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean overMain = super.isMouseOver(mouseX, mouseY);
        if (inputBox.visible) {
            double ix = inputBox.getX();
            double iy = inputBox.getY();
            double iw = inputBox.getWidth();
            double ih = inputBox.getHeight();
            if (mouseX >= ix && mouseX <= ix + iw && mouseY >= iy && mouseY <= iy + ih) return true;
        }
        if (activeDropdown != null) {
            for (DropdownEntry entry : dropdowns) {
                if (entry.dropdown == activeDropdown) {
                    int menuHeight = activeDropdown.getOptions().values.size() * 15;
                    boolean overMenu = mouseX >= activeDropdown.getX() && mouseX <= activeDropdown.getX() + 80 && mouseY >= activeDropdown.getY() + 20 && mouseY <= activeDropdown.getY() + 20 + menuHeight;
                    return overMain || overMenu;
                }
            }
        }
        return overMain;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputBox.visible && inputBox.isFocused()) {
            if (inputBox.keyPressed(keyCode, scanCode, modifiers)) {
                this.currentMacro.actions.get(this.actionID).value(inputBox.getValue());
                FileHandler.updateMacroInFile(this.currentMacro);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (inputBox.visible && inputBox.isFocused()) {
            if (inputBox.charTyped(codePoint, modifiers)) {
                this.currentMacro.actions.get(this.actionID).value(inputBox.getValue());
                FileHandler.updateMacroInFile(this.currentMacro);
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }


}
