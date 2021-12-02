package dev.cerus.hardcorehearts;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class HardcoreHeartsPlugin extends JavaPlugin {

    private boolean heartsEnabled;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.heartsEnabled = this.getConfig().getBoolean("enabled");

        this.getServer().getPluginManager().registerEvents(new LoginListener(this), this);

        this.getCommand("hardcorehearts").setExecutor(this);
        this.getCommand("hardcorehearts").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            return false;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("toggle")) {
            this.setHeartsEnabled(!this.isHeartsEnabled());
            sender.sendMessage("§8[§cHardcore Hearts§8] §7The hardcore hearts are now " + (this.heartsEnabled ? "§aenabled" : "§cdisabled"));
            sender.sendMessage("§8[§cHardcore Hearts§8] §7You will have to rejoin to see the changes.");
            return true;
        }

        sender.sendMessage("§8[§cHardcore Hearts§8] §7The hardcore hearts are " + (this.heartsEnabled ? "§aenabled" : "§cdisabled"));
        sender.sendMessage("§8[§cHardcore Hearts§8] §7Type §b/hardcorehearts toggle §7to enable/disable");
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return args.length <= 1 ? Collections.singletonList("toggle") : List.of();
    }

    public boolean isHeartsEnabled() {
        return this.heartsEnabled;
    }

    public void setHeartsEnabled(final boolean heartsEnabled) {
        this.heartsEnabled = heartsEnabled;

        this.getConfig().set("enabled", this.heartsEnabled);
        this.saveConfig();
    }

}
