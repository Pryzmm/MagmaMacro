package com.pryzmm.magmamacro.platform;

import com.pryzmm.magmamacro.Constants;
import com.pryzmm.magmamacro.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        Constants.CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
