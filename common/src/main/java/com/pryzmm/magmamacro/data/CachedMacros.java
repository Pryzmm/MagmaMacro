package com.pryzmm.magmamacro.data;

import java.util.ArrayList;
import java.util.List;

public class CachedMacros {

    public static List<FileHandler.Macro> cachedMacros = new ArrayList<>();

    public static void addToCache(FileHandler.Macro macro) throws Exception {
        if (cachedMacros.contains(macro)) throw new Exception("Cache already contains a similar macro and cannot be added to the cache.");
        else cachedMacros.add(macro);
    }

    public static void removeFromCache(FileHandler.Macro macro) {
        cachedMacros.remove(macro);
    }

    public static void refreshCache(FileHandler.MacroFile file) {
        cachedMacros.clear();
        cachedMacros.addAll(FileHandler.getMacros(file));
    }

}
