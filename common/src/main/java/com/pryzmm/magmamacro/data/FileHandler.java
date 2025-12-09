package com.pryzmm.magmamacro.data;

import com.google.gson.Gson;
import com.pryzmm.magmamacro.Constants;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class FileHandler {

    static Gson gson = new Gson();

    public static final class MacroAction {
        private String action;
        private String value;
        private String direction;
        private Integer length;
        private Integer delay;

        public MacroAction(String action, String value, String direction, Integer length, Integer delay) {
            this.action = action;
            this.value = value;
            this.direction = direction;
            this.length = length;
            this.delay = delay;
        }

        public String action() {
            return action;
        }
        public void action(String action) {
            this.action = action;
        }

        public String value() {
            return value;
        }
        public void value(String value) {
            this.value = value;
        }

        public String direction() {
            return direction;
        }
        public void direction(String direction) {
            this.direction = direction;
        }

        public Integer length() {
            return length;
        }
        public void length(Integer length) {
            this.length = length;
        }

        public Integer delay() {
            return delay;
        }
        public void delay(Integer delay) {
            this.delay = delay;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MacroAction) obj;
            return Objects.equals(this.action, that.action) &&
                    Objects.equals(this.value, that.value) &&
                    Objects.equals(this.direction, that.direction) &&
                    Objects.equals(this.length, that.length) &&
                    Objects.equals(this.delay, that.delay);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, value, direction, length, delay);
        }

        @Override
        public String toString() {
            return "MacroAction[" +
                    "action=" + action + ", " +
                    "value=" + value + ", " +
                    "direction=" + direction + ", " +
                    "length=" + length + ", " +
                    "delay=" + delay + ']';
        }
    }

    public static class Macro {
        public String name;
        public String bind;
        public Integer id;
        public Map<Integer, MacroAction> actions; // Maps "1" -> action, "2" -> action
    }

    public static class MacroFile {
        public HashMap<Integer, Macro> macros;
    }

    public static MacroFile file = loadMacroFile();

    public static Path getDataFile() { return Constants.CONFIG_DIR.resolve("magmamacro/data.json"); }

    public static Macro addBlankMacroToFile() {
        Macro blankMacro = new Macro();
        blankMacro.name = "New Macro";
        blankMacro.actions = new HashMap<>();
        MacroFile macroFile = loadMacroFile();
        Integer newId = macroFile.macros.keySet().stream()
                .max(Integer::compareTo)
                .map(maxId -> maxId + 1)
                .orElse(1);
        blankMacro.id = newId;
        macroFile.macros.put(newId, blankMacro);
        addBlankAction(macroFile, newId);
        return blankMacro;
    }

    public static void updateMacroInFile(Macro macro) {
        if (macro == null) return;
        file.macros.put(macro.id, macro);
        saveMacroFile(file);
    }

    public static MacroFile loadMacroFile() {
        File file = getDataFile().toFile();
        if (!file.exists()) {
            MacroFile newFile = new MacroFile();
            newFile.macros = new HashMap<>();
            return newFile;
        }
        try (FileReader reader = new FileReader(file)) {
            MacroFile macroFile = gson.fromJson(reader, MacroFile.class);
            if (macroFile == null || macroFile.macros == null) {
                macroFile = new MacroFile();
                macroFile.macros = new HashMap<>();
            }
            CachedMacros.refreshCache(macroFile);
            return macroFile;
        } catch (Exception e) {
            Constants.LOG.error("Failed to load macro file", e);
            MacroFile emptyFile = new MacroFile();
            emptyFile.macros = new HashMap<>();
            return emptyFile;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveMacroFile(MacroFile macroFile) {
        File file = getDataFile().toFile();
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(macroFile, writer);
            Constants.LOG.info("Saved macro file successfully");
        } catch (Exception e) {
            Constants.LOG.error("Failed to save macro file", e);
        }
    }

    public static Collection<Macro> getMacros(MacroFile file) { return file.macros.values(); }

    public static void addBlankAction(MacroFile file, Integer macroID) {
        Macro macro = file.macros.get(macroID);
        macro.actions.put(macro.actions.size() + 1, new MacroAction("ACTION", "VALUE", "DIRECTION", 0, 0));
    }

}
