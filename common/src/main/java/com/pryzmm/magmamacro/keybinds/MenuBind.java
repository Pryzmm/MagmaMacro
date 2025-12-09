package com.pryzmm.magmamacro.keybinds;

import com.pryzmm.magmamacro.screen.MacroScreen;
import net.minecraft.client.Minecraft;

public class MenuBind {

    public static void openMenu() {
        Minecraft.getInstance().setScreen(new MacroScreen());
    }

}
