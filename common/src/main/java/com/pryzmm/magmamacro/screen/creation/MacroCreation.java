package com.pryzmm.magmamacro.screen.creation;

import com.pryzmm.magmamacro.Constants;
import com.pryzmm.magmamacro.data.FileHandler;
import com.pryzmm.magmamacro.screen.MacroScreen;
import com.pryzmm.magmamacro.screen.util.ScrollableList;
import com.pryzmm.magmamacro.screen.util.TaskDisplay;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class MacroCreation {

    public static void createNewMacro() {
        FileHandler.Macro macro = FileHandler.addBlankMacroToFile();
        MacroScreen.macroList.children().add(new ScrollableList.Entry(Button.builder(Component.translatable("button.magmamacro.new_macro"), button -> openMacro(macro)).build(), macro));
    }

    public static void openMacro(FileHandler.Macro macro) {
        if (MacroScreen.selectedMacro != null) FileHandler.updateMacroInFile(MacroScreen.selectedMacro);
        MacroScreen.taskList.children().clear();
        Constants.LOG.info("Opening macro: {} (ID: {})", macro.name, macro.id);
        AbstractSelectionList<ScrollableList.Entry> taskList = MacroScreen.taskList;
        MacroScreen.selectedMacro = macro;
        for (int i : macro.actions.keySet()) {
            FileHandler.MacroAction action = macro.actions.get(i);
            taskList.children().add(new ScrollableList.Entry(new TaskDisplay(taskList.getX() + 5, taskList.getWidth() - 16, 28, i, action.action(), action.value(), action.direction(), action.length(), action.delay(), macro)));
        }
    }

}
