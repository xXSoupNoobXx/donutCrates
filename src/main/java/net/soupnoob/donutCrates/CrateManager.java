package net.soupnoob.donutCrates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class CrateManager {
   private final DonutCrates plugin;
   public final Map<String, Block> crateBlocks = new HashMap<>();

   public CrateManager(DonutCrates pl) {
      this.plugin = pl;
      ConfigurationSection ss = this.plugin.cfg.saves.getConfigurationSection("crateBlocks");
      if (ss != null) {
         Iterator<String> it = ss.getKeys(false).iterator();

         while (it.hasNext()) {
            String name = it.next();
            String[] p = ss.getString(name).split(",");
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               World w = Bukkit.getWorld(p[0]);
               if (w != null) {
                  Block b = w.getBlockAt(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                  this.crateBlocks.put(name, b);
               }
            }, 1L);
         }
      }
   }

   public boolean crateExists(String n) {
      return this.crateBlocks.containsKey(n);
   }

   public void saveBlocks() {
      ConfigurationSection ss = this.plugin.cfg.saves.createSection("crateBlocks");
      this.crateBlocks.forEach((n, b) -> {
         String var10002 = b.getWorld().getName();
         ss.set(n, var10002 + "," + b.getX() + "," + b.getY() + "," + b.getZ());
      });
      this.plugin.cfg.saveAll();
   }

   public void createCrate(String name, Block b, Player p) {
      this.crateBlocks.put(name, b);
      String base = "Crates." + name;
      this.plugin.cfg.crates.createSection(base);
      this.plugin.cfg.crates.set(base + ".rows", 3);
      this.plugin.cfg.crates.set(base + ".fillerEnabled", true);
      this.plugin.cfg.crates.set(base + ".fillerMaterial", this.plugin.cfg.config.getString("fillerMaterial"));
      this.plugin.cfg.crates.set(base + ".title", "&#91b8ffᴄʜᴏᴏѕᴇ 1 ɪᴛᴇᴍ");
      ConfigurationSection items = this.plugin.cfg.crates.createSection(base + ".Items");
      items.createSection("Item1").set("material", "DIAMOND_SWORD");
      this.plugin.cfg.crates.set(base + ".Items.Item1.displayname", "&aCommon Sword");
      this.plugin.cfg.crates.set(base + ".Items.Item1.command", "");
      this.plugin.cfg.crates.set(base + ".Items.Item1.amount", 1);
      this.plugin.cfg.crates.set(base + ".Items.Item1.slot", 10);
      this.plugin.cfg.crates.set(base + ".Items.Item1.giveitem", true);
      this.plugin.cfg.crates.set(base + ".Items.Item1.lore", List.of("&fExample Line 1", "&fExample Line 2"));
      this.plugin.cfg.crates.set(base + ".Items.Item1.enchantments", List.of("UNBREAKING;3", "MENDING;1"));
      this.plugin.cfg.saveAll();
      this.saveBlocks();
      p.sendMessage(Utils.formatColors("&aCreated crate &e" + name + "&a at your target block."));
   }

   public void deleteCrate(String name, Player p) {
      this.crateBlocks.remove(name);
      this.plugin.cfg.crates.set("Crates." + name, (Object) null);
      this.plugin.cfg.saveAll();
      this.saveBlocks();
      p.sendMessage(Utils.formatColors("&cDeleted crate &e" + name));
   }

   public void moveCrate(String name, Block b, Player p) {
      this.crateBlocks.put(name, b);
      this.saveBlocks();
      p.sendMessage(Utils.formatColors("&aMoved crate &e" + name + "&a."));
   }

   public void removeItem(String crate, String key, Player p) {
      String path = "Crates." + crate + ".Items." + key;
      if (this.plugin.cfg.crates.getConfigurationSection(path) == null) {
         p.sendMessage(Utils.formatColors("&cItem &e" + key + "&c not found."));
      } else {
         this.plugin.cfg.crates.set(path, (Object) null);
         this.plugin.cfg.saveAll();
         p.sendMessage(Utils.formatColors("&cRemoved item &e" + key + "&c."));
      }
   }

   public void addHandItem(String crate, Player p) {
      ItemStack item = p.getInventory().getItemInMainHand();
      if (item != null && !item.getType().isAir()) {
         ConfigurationSection sec = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
         if (sec == null) {
            p.sendMessage(Utils.formatColors("&cNo crate &e" + crate));
         } else {
            int idx = sec.getKeys(false).stream().mapToInt((k) -> {
               return Integer.parseInt(k.replaceAll("\\D", ""));
            }).max().orElse(0) + 1;
            String key = "Item" + idx;
            String base = "Crates." + crate + ".Items." + key;
            this.plugin.cfg.crates.set(base + ".material", item.getType().name());
            ItemMeta meta = item.getItemMeta();
            this.plugin.cfg.crates.set(base + ".displayname", meta != null ? meta.getDisplayName() : "");
            this.plugin.cfg.crates.set(base + ".amount", item.getAmount());
            this.plugin.cfg.crates.set(base + ".slot", idx + 8);
            this.plugin.cfg.crates.set(base + ".lore", meta != null ? meta.getLore() : null);
            this.plugin.cfg.crates.set(base + ".command", "");
            this.plugin.cfg.crates.set(base + ".giveitem", true);

            List<String> enchants = item.getEnchantments().entrySet().stream().map((e) -> {
               return enchantmentToString(e.getKey()) + ";" + e.getValue();
            }).toList();
            this.plugin.cfg.crates.set(base + ".enchantments", enchants);

            if (meta != null && meta.isUnbreakable()) {
               this.plugin.cfg.crates.set(base + ".unbreakable", true);
            }

            if (meta instanceof ArmorMeta) {
               ArmorMeta am = (ArmorMeta) meta;
               if (am.hasTrim()) {
                  ArmorTrim trim = am.getTrim();
                  this.plugin.cfg.crates.set(base + ".trim.material", trimKeyToString(trim.getMaterial()));
                  this.plugin.cfg.crates.set(base + ".trim.pattern", trimKeyToString(trim.getPattern()));
               }
            }

            this.plugin.cfg.saveAll();
            p.sendMessage(Utils.formatColors("&aAdded hand item as &e" + key + "&a."));
         }
      } else {
         p.sendMessage(Utils.formatColors("&cHold an item."));
      }
   }

   public boolean isCrateBlock(Block b) {
      return this.crateBlocks.containsValue(b);
   }

   public String getCrateByBlock(Block b) {
      return this.crateBlocks.entrySet().stream().filter((e) -> {
         return e.getValue().equals(b);
      }).map(Entry::getKey).findFirst().orElse(null);
   }

   private static String enchantmentToString(Enchantment e) {
      if (e == null) return null;
      try {
         return e.getKey() != null ? e.getKey().toString() : e.getName();
      } catch (NoSuchMethodError | AbstractMethodError ex) {
         return e.getName();
      }
   }

   private static String trimKeyToString(TrimMaterial material) {
      if (material == null) return null;
      try {
         var m = material.getClass().getMethod("getKey");
         Object key = m.invoke(material);
         return key != null ? key.toString() : material.toString();
      } catch (ReflectiveOperationException | NoSuchMethodError | AbstractMethodError ex) {
         return material.toString();
      }
   }

   private static String trimKeyToString(TrimPattern pattern) {
      if (pattern == null) return null;
      try {
         var m = pattern.getClass().getMethod("getKey");
         Object key = m.invoke(pattern);
         return key != null ? key.toString() : pattern.toString();
      } catch (ReflectiveOperationException | NoSuchMethodError | AbstractMethodError ex) {
         return pattern.toString();
      }
   }
}
