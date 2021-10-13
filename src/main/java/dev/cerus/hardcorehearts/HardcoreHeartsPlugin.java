package dev.cerus.hardcorehearts;

import org.bukkit.plugin.java.JavaPlugin;

public class HardcoreHeartsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new LoginListener(this.getLogger()), this);
    }

}
