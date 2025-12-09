package com.pryzmm.magmamacro;

import com.pryzmm.magmamacro.data.FileHandler;
import com.pryzmm.magmamacro.keybinds.MacroRunner;
import com.pryzmm.magmamacro.platform.Services;
import com.pryzmm.magmamacro.scheduler.ClientScheduler;

public class CommonClass {

    public static final String KEY_CATEGORY_MAGMA_MACRO = "key.category.magmamacro";
    public static final String KEY_MACRO_OPEN = "key.magmamacro.open_macro";

    public static final ClientScheduler CLIENT_SCHEDULER = new ClientScheduler();

    public static void init() {
        Services.PLATFORM.getPlatformName();
        FileHandler.loadMacroFile();
        MacroRunner.refreshBinds();
    }

}
