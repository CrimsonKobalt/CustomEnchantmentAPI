package db.chris.customenchantment;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

public class Main extends JavaPlugin {
    FileConfiguration config = this.getConfig();

    public Main() {
        super();
    }

    public Main(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    @Override
    public void onEnable() {
        CustomEnchantmentAPI.start(this, config);
        this.setConfiguration();
    }

    private void setConfiguration() {
        config.addDefault("anvil.fix.enabled", true);
        config.options().copyDefaults(true);
        this.saveConfig();
    }
}
