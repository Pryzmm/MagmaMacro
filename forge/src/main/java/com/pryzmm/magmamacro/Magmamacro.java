package com.pryzmm.magmamacro;

import com.mojang.blaze3d.platform.InputConstants;
import com.pryzmm.magmamacro.keybinds.MacroRunner;
import com.pryzmm.magmamacro.keybinds.MenuBind;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.lang.invoke.MethodHandles;

@Mod(Constants.MOD_ID)
public class Magmamacro {

    public static final KeyMapping openMacroKeybind = new KeyMapping(
            CommonClass.KEY_MACRO_OPEN,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CommonClass.KEY_CATEGORY_MAGMA_MACRO
    );

    public Magmamacro(FMLJavaModLoadingContext context) {
        Constants.LOG.info("Loading Magma Macro for Forge");
        CommonClass.init();

        context.getModBusGroup().register(MethodHandles.lookup(), this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openMacroKeybind);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.PlayerTickEvent.Post event) {
        while (openMacroKeybind.consumeClick()) MenuBind.openMenu();
        MacroRunner.onClientTick();
        CommonClass.CLIENT_SCHEDULER.clientTick();
    }

}