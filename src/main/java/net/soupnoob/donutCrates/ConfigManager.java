package net.soupnoob.donutCrates;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
   public FileConfiguration config;
   public FileConfiguration crates;
   public FileConfiguration saves;
   private final File cfgFile;
   private final File cratesFile;
   private final File savesFile;

   public ConfigManager(DonutCrates plugin) {
      this.cfgFile = new File(plugin.getDataFolder(), "config.yml");
      this.cratesFile = new File(plugin.getDataFolder(), "crates.yml");
      this.savesFile = new File(plugin.getDataFolder(), "saves.yml");
      plugin.saveResource("config.yml", false);
      plugin.saveResource("crates.yml", false);
      plugin.saveResource("saves.yml", false);
      this.reloadAll();
   }

   public void reloadAll() {
      this.config = YamlConfiguration.loadConfiguration(this.cfgFile);
      this.crates = YamlConfiguration.loadConfiguration(this.cratesFile);
      this.saves = YamlConfiguration.loadConfiguration(this.savesFile);
   }

   public void saveAll() {
      try {
         this.config.save(this.cfgFile);
         this.crates.save(this.cratesFile);
         this.saves.save(this.savesFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }
}
