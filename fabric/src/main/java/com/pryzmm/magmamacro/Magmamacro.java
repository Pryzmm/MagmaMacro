package com.pryzmm.magmamacro;

import com.pryzmm.magmamacro.keybinds.MacroRunner;
import com.pryzmm.magmamacro.keybinds.MenuBind;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Magmamacro implements ModInitializer {

    private static KeyMapping openMacroKeybind;

    @Override
    public void onInitialize() {
        Constants.LOG.info("Loading Magma Macro for Fabric");
        CommonClass.init();

        openMacroKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                CommonClass.KEY_MACRO_OPEN,
                GLFW.GLFW_KEY_M,
                CommonClass.KEY_CATEGORY_MAGMA_MACRO
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMacroKeybind.consumeClick()) MenuBind.openMenu();
            MacroRunner.onClientTick();
            CommonClass.CLIENT_SCHEDULER.clientTick();
        });
    }
}