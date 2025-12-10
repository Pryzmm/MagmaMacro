package com.pryzmm.magmamacro.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.pryzmm.magmamacro.CommonClass;
import com.pryzmm.magmamacro.Constants;
import com.pryzmm.magmamacro.data.FileHandler;
import com.pryzmm.magmamacro.scheduler.ClientScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.*;

public class MacroRunner {

    public static Map<String, List<Integer>> macroBinds = new HashMap<>();
    private static final Map<String, Boolean> keyStates = new HashMap<>();

    public static void refreshBinds() {
        macroBinds.clear();
        keyStates.clear();
        FileHandler.MacroFile file = FileHandler.loadMacroFile();
        for (FileHandler.Macro macro : FileHandler.getMacros(file)) {
            String bindKey = macro.bind;
            if (bindKey != null && !bindKey.isEmpty()) {
                addBindToMacro(macro.id, bindKey);
            }
        }
    }

    public static void addBindToMacro(Integer macroID, String key) {
        for (String existingKey : macroBinds.keySet()) {
            List<Integer> boundMacros = macroBinds.get(existingKey);
            if (boundMacros.contains(macroID)) {
                boundMacros.remove(macroID);
                macroBinds.put(existingKey, boundMacros);
            }
        }
        if (macroBinds.containsKey(key)) {
            List<Integer> boundMacros = macroBinds.get(key);
            if (!boundMacros.contains(macroID)) {
                boundMacros.add(macroID);
                macroBinds.put(key, boundMacros);
            }
        } else {
            macroBinds.put(key, new ArrayList<>(List.of(macroID)));
        }
    }

    public static void removeBindFromMacro(Integer macroID, String key) {
        if (macroBinds.containsKey(key)) {
            List<Integer> boundMacros = macroBinds.get(key);
            if (boundMacros.contains(macroID)) {
                boundMacros.remove(macroID);
                macroBinds.put(key, boundMacros);
            }
        }
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.screen != null) return;
        long window = mc.getWindow().getWindow();
        for (String keyName : macroBinds.keySet()) {
            InputConstants.Key key = InputConstants.getKey(keyName);
            boolean isPressed = InputConstants.isKeyDown(window, key.getValue());
            boolean wasPressed = keyStates.getOrDefault(keyName, false);
            if (isPressed && !wasPressed) runMacrosForKey(keyName);
            keyStates.put(keyName, isPressed);
        }
        macroTick();
    }

    private static final Map<String, Integer> activeMovements = new HashMap<>();

    public static void runMacrosForKey(String key) {
        if (macroBinds.containsKey(key)) {
            List<Integer> boundMacros = macroBinds.get(key);
            for (Integer macroID : boundMacros) {
                FileHandler.Macro macro = FileHandler.file.macros.get(macroID);
                if (macro != null) {
                    Minecraft instance = Minecraft.getInstance();
                    int actionDelay = 0;
                    for (FileHandler.MacroAction action : macro.actions.values()) {
                        actionDelay += (action.delay() != null) ? action.delay() : 0;
                        ClientScheduler.TaskHandle handle = CommonClass.CLIENT_SCHEDULER.runClientTaskLater(v -> {
                            LocalPlayer player = instance.player;
                            if (instance.player != null) {
                                switch (action.action()) {
                                    case "COMMAND" -> player.connection.sendCommand(action.value());
                                    case "CHAT" -> player.connection.sendChat(action.value());
                                    case "MOVEMENT" -> {
                                        if (action.length() != null && action.direction() != null) {
                                            int durationTicks = action.length();
                                            String direction = action.direction();
                                            activeMovements.put(direction, durationTicks);
                                        }
                                    }
                                }
                            } else {
                                Constants.LOG.warn("Player is null, cannot run macro actions.");
                            }
                        }, actionDelay);
                    }
                } else {
                    Constants.LOG.warn("Macro with ID: {} not found in file.", macroID);
                }
            }
        }
    }

    public static void macroTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        Iterator<Map.Entry<String, Integer>> iter = activeMovements.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            String direction = entry.getKey();
            int ticksRemaining = entry.getValue();
            if (ticksRemaining > 0) {
                switch (direction) {
                    case "FORWARDS" -> mc.options.keyUp.setDown(true);
                    case "BACKWARDS" -> mc.options.keyDown.setDown(true);
                    case "LEFT" -> mc.options.keyLeft.setDown(true);
                    case "RIGHT" -> mc.options.keyRight.setDown(true);
                }
                entry.setValue(ticksRemaining - 1);
            } else {
                switch (direction) {
                    case "FORWARDS" -> mc.options.keyUp.setDown(false);
                    case "BACKWARDS" -> mc.options.keyDown.setDown(false);
                    case "LEFT" -> mc.options.keyLeft.setDown(false);
                    case "RIGHT" -> mc.options.keyRight.setDown(false);
                }
                iter.remove();
            }
        }
    }

}