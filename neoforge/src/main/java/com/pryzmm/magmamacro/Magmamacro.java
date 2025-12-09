package com.pryzmm.magmamacro;

import com.mojang.blaze3d.platform.InputConstants;
import com.pryzmm.magmamacro.keybinds.MacroRunner;
import com.pryzmm.magmamacro.keybinds.MenuBind;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(Constants.MOD_ID)
public class Magmamacro {

    public static final KeyMapping openMacroKeybind = new KeyMapping(
        CommonClass.KEY_MACRO_OPEN,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        CommonClass.KEY_CATEGORY_MAGMA_MACRO
    );

    public Magmamacro(IEventBus eventBus) {
        Constants.LOG.info("Loading Magma Macro for NeoForge");
        CommonClass.init();

        eventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.register(this);
    }

    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openMacroKeybind);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        while (openMacroKeybind.consumeClick()) MenuBind.openMenu();
        MacroRunner.onClientTick();
        CommonClass.CLIENT_SCHEDULER.clientTick();
    }


}
