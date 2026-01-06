package net.soupnoob.donutCrates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class GUIManager {
   private final DonutCrates plugin;

   public GUIManager(DonutCrates pl) {
      this.plugin = pl;
   }

   private Material safeMaterial(String name, Material fallback) {
      if (name == null) return fallback;
      try {
         return Material.valueOf(name);
      } catch (Exception ex) {
         return fallback;
      }
   }

   public Inventory buildCrateGUI(String crateName) {
      ConfigurationSection sec = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateName);
      if (sec == null) {
         return Bukkit.createInventory((InventoryHolder) null, 9, Utils.formatColors("&cInvalid crate"));
      }

      String title = Utils.formatColors(sec.getString("title", crateName).replace("%crate%", crateName));
      int rows = Math.max(1, Math.min(6, sec.getInt("rows", 3))); // sanity bound
      boolean fill = sec.getBoolean("fillerEnabled", false);
      Material filler = safeMaterial(sec.getString("fillerMaterial", "GRAY_STAINED_GLASS_PANE"), Material.GRAY_STAINED_GLASS_PANE);
      CrateHolder holder = new CrateHolder(crateName);
      Inventory inv = Bukkit.createInventory(holder, rows * 9, title);
      holder.setInventory(inv);
      if (fill) {
         ItemStack f = new ItemStack(filler);
         ItemMeta fm = f.getItemMeta();
         if (fm != null) {
            fm.setDisplayName(" ");
            f.setItemMeta(fm);
         }

         for (int i = 0; i < rows * 9; ++i) {
            inv.setItem(i, f);
         }
      }

      ConfigurationSection items = sec.getConfigurationSection("Items");
      if (items != null) {
         for (String key : items.getKeys(false)) {
            ConfigurationSection it = items.getConfigurationSection(key);
            if (it == null) continue;

            Material mat = safeMaterial(it.getString("material", "STONE"), Material.STONE);
            int amount = it.getInt("amount", 1);
            ItemStack is = new ItemStack(mat, amount);
            ItemMeta im = is.getItemMeta();
            if (im != null) {
               im.setDisplayName(Utils.formatColors(it.getString("displayname", "")));
               im.setLore(Utils.formatColors(it.getStringList("lore")));

               for (String matKey : it.getStringList("enchantments")) {
                  String[] parts = matKey.split(";");
                  if (parts.length < 2) continue;
                  Enchantment e = Enchantment.getByName(parts[0].trim());
                  try {
                     int lvl = Integer.parseInt(parts[1].trim());
                     if (e != null) im.addEnchant(e, lvl, true);
                  } catch (NumberFormatException ignored) {}
               }

               if (it.getBoolean("unbreakable", false)) {
                  im.setUnbreakable(true);
               }

               if (im instanceof ArmorMeta) {
                  ArmorMeta am = (ArmorMeta) im;
                  if (it.isConfigurationSection("trim")) {
                     String matKey = it.getString("trim.material");
                     String patternKey = it.getString("trim.pattern");
                     try {
                        NamespacedKey mns = NamespacedKey.fromString(matKey);
                        NamespacedKey pns = NamespacedKey.fromString(patternKey);
                        TrimMaterial tm = mns == null ? null : (TrimMaterial) Registry.TRIM_MATERIAL.get(mns);
                        TrimPattern tp = pns == null ? null : (TrimPattern) Registry.TRIM_PATTERN.get(pns);
                        if (tm != null && tp != null) {
                           ArmorTrim trim = new ArmorTrim(tm, tp);
                           am.setTrim(trim);
                        }
                     } catch (Exception var25) {
                        this.plugin.getLogger().warning("Invalid trim in crate '" + crateName + "/" + key + "': " + var25.getMessage());
                     }
                     im = am;
                  }
               }

               is.setItemMeta(im);
            }
            int slot = it.getInt("slot", 0);
            if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, is);
         }
      }

      return inv;
   }

   public Inventory buildConfirmGUI(Player p, ItemStack clicked, String crateName) {
      ConfigurationSection c = this.plugin.cfg.config.getConfigurationSection("confirm-menu");
      if (c == null) {
         return Bukkit.createInventory((InventoryHolder) null, 9, Utils.formatColors("&cInvalid menu"));
      }

      int rows = Math.max(1, Math.min(6, c.getInt("rows", 3)));
      boolean fill = c.getBoolean("fillerEnabled", false);
      Material filler = safeMaterial(c.getString("fillerMaterial", "GRAY_STAINED_GLASS_PANE"), Material.GRAY_STAINED_GLASS_PANE);
      Inventory inv = Bukkit.createInventory((InventoryHolder) null, rows * 9, Utils.formatColors(c.getString("title")));
      if (fill) {
         ItemStack f = new ItemStack(filler);
         ItemMeta cim = f.getItemMeta();
         if (cim != null) {
            cim.setDisplayName(" ");
            f.setItemMeta(cim);
         }
         for (int i = 0; i < rows * 9; ++i) inv.setItem(i, f);
      }

      ItemStack f = clicked == null ? new ItemStack(Material.STONE) : clicked.clone();
      ItemMeta cim = f.getItemMeta();
      String clickedName = "";
      if (clicked != null && clicked.getItemMeta() != null && clicked.getItemMeta().getDisplayName() != null) {
         clickedName = clicked.getItemMeta().getDisplayName();
      }
      if (cim != null) {
         cim.setDisplayName(Utils.formatColors(c.getString("ClickedItem.displayname", "%ClickedItemName%").replace("%ClickedItemName%", clickedName)));
         f.setItemMeta(cim);
      }
      int clickedSlot = c.getInt("ClickedItem.slot", 13);
      if (clickedSlot >= 0 && clickedSlot < inv.getSize()) inv.setItem(clickedSlot, f);

      ItemStack con = new ItemStack(safeMaterial(c.getString("Confirm.material", "LIME_STAINED_GLASS_PANE"), Material.LIME_STAINED_GLASS_PANE));
      ItemMeta cm = con.getItemMeta();
      if (cm != null) {
         cm.setDisplayName(Utils.formatColors(c.getString("Confirm.displayname")));
         cm.setLore(Utils.formatColors(c.getStringList("Confirm.lore")));
         con.setItemMeta(cm);
      }
      int confirmSlot = c.getInt("Confirm.slot", 15);
      if (confirmSlot >= 0 && confirmSlot < inv.getSize()) inv.setItem(confirmSlot, con);

      ItemStack dec = new ItemStack(safeMaterial(c.getString("Decline.material", "RED_STAINED_GLASS_PANE"), Material.RED_STAINED_GLASS_PANE));
      ItemMeta dm = dec.getItemMeta();
      if (dm != null) {
         dm.setDisplayName(Utils.formatColors(c.getString("Decline.displayname")));
         dm.setLore(Utils.formatColors(c.getStringList("Decline.lore")));
         dec.setItemMeta(dm);
      }
      int declineSlot = c.getInt("Decline.slot", 11);
      if (declineSlot >= 0 && declineSlot < inv.getSize()) inv.setItem(declineSlot, dec);

      return inv;
   }

   public Inventory buildMainEditorGUI() {
      int rows = 6;
      Inventory inv = Bukkit.createInventory((InventoryHolder) null, rows * 9, Utils.formatColors("&6Crate Editor"));
      int idx = 0;
      int max = inv.getSize();
      Set<String> keys = this.plugin.crateMgr.crateBlocks.keySet();
      for (String crate : keys) {
         if (idx >= max) break; // avoid overflow
         Block block = this.plugin.crateMgr.crateBlocks.get(crate);
         Material mat = block != null ? block.getType() : Material.STONE;
         ItemStack is = new ItemStack(mat);
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            im.setDisplayName(Utils.formatColors(crate));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(Utils.formatColors("&7Click to edit crate"));
            lore.add(Utils.formatColors("&7Shift + Right-click to delete crate"));
            im.setLore(lore);
            is.setItemMeta(im);
         }
         inv.setItem(idx++, is);
      }

      return inv;
   }

   public Inventory buildCrateEditorGUI(String crateName) {
      int rows = 6;
      Inventory inv = Bukkit.createInventory((InventoryHolder) null, rows * 9, Utils.formatColors(crateName + " Editor"));
      ConfigurationSection items = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateName + ".Items");
      if (items != null) {
         for (String key : items.getKeys(false)) {
            ConfigurationSection it = items.getConfigurationSection(key);
            if (it == null) continue;
            Material mat = safeMaterial(it.getString("material", "STONE"), Material.STONE);
            int slot = it.getInt("slot", 0);
            int amount = it.getInt("amount", 1);
            ItemStack is = new ItemStack(mat, amount);
            ItemMeta im = is.getItemMeta();
            if (im != null) {
               im.setDisplayName(Utils.formatColors(it.getString("displayname", "")));
               List<String> lore = new ArrayList<>(Utils.formatColors(it.getStringList("lore")));
               lore.add("");
               lore.add(Utils.formatColors("&7Click to edit item!"));
               lore.add("");
               lore.add(Utils.formatColors("&7Shift + Left-click to move Left"));
               lore.add(Utils.formatColors("&7Shift + Right-click to move Right"));
               im.setLore(lore);

               for (String matKey : it.getStringList("enchantments")) {
                  String[] parts = matKey.split(";");
                  if (parts.length < 2) continue;
                  Enchantment e = Enchantment.getByName(parts[0].trim());
                  try {
                     int lvl = Integer.parseInt(parts[1].trim());
                     if (e != null) im.addEnchant(e, lvl, true);
                  } catch (NumberFormatException ignored) {}
               }

               if (it.getBoolean("unbreakable", false)) im.setUnbreakable(true);

               if (im instanceof ArmorMeta) {
                  ArmorMeta am = (ArmorMeta) im;
                  if (it.isConfigurationSection("trim")) {
                     String matKey = it.getString("trim.material");
                     String patternKey = it.getString("trim.pattern");
                     try {
                        NamespacedKey mns = NamespacedKey.fromString(matKey);
                        NamespacedKey pns = NamespacedKey.fromString(patternKey);
                        TrimMaterial tm = mns == null ? null : (TrimMaterial) Registry.TRIM_MATERIAL.get(mns);
                        TrimPattern tp = pns == null ? null : (TrimPattern) Registry.TRIM_PATTERN.get(pns);
                        if (tm != null && tp != null) {
                           ArmorTrim trim = new ArmorTrim(tm, tp);
                           am.setTrim(trim);
                        }
                     } catch (Exception var22) {
                        this.plugin.getLogger().warning("Invalid trim in editor '" + crateName + "/" + key + "': " + var22.getMessage());
                     }
                     im = am;
                  }
               }

               is.setItemMeta(im);
            }
            if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, is);
         }
      }

      ItemStack back = new ItemStack(Material.ARROW);
      ItemMeta bm = back.getItemMeta();
      if (bm != null) {
         bm.setDisplayName(Utils.formatColors("&eBack to Crates"));
         back.setItemMeta(bm);
      }
      inv.setItem(45, back);
      return inv;
   }

   public Inventory buildEditorItemMenu(String crateName, String itemKey) {
      ConfigurationSection it = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateName + ".Items." + itemKey);
      if (it == null) {
         return Bukkit.createInventory((InventoryHolder) null, 9, Utils.formatColors("&cInvalid item"));
      }
      Material mat = safeMaterial(it.getString("material", "STONE"), Material.STONE);
      int amount = it.getInt("amount", 1);
      ItemStack base = new ItemStack(mat, amount);
      ItemMeta bim = base.getItemMeta();
      if (bim != null) {
         bim.setDisplayName(Utils.formatColors(it.getString("displayname", "")));
         bim.setLore(Utils.formatColors(it.getStringList("lore")));

         for (String matKey : it.getStringList("enchantments")) {
            String[] parts = matKey.split(";");
            if (parts.length < 2) continue;
            Enchantment e = Enchantment.getByName(parts[0].trim());
            try {
               int lvl = Integer.parseInt(parts[1].trim());
               if (e != null) bim.addEnchant(e, lvl, true);
            } catch (NumberFormatException ignored) {}
         }

         if (it.getBoolean("unbreakable", false)) bim.setUnbreakable(true);

         if (bim instanceof ArmorMeta) {
            ArmorMeta am = (ArmorMeta) bim;
            if (it.isConfigurationSection("trim")) {
               String matKey = it.getString("trim.material");
               String patternKey = it.getString("trim.pattern");
               try {
                  NamespacedKey mns = NamespacedKey.fromString(matKey);
                  NamespacedKey pns = NamespacedKey.fromString(patternKey);
                  TrimMaterial tm = mns == null ? null : (TrimMaterial) Registry.TRIM_MATERIAL.get(mns);
                  TrimPattern tp = pns == null ? null : (TrimPattern) Registry.TRIM_PATTERN.get(pns);
                  if (tm != null && tp != null) {
                     ArmorTrim trim = new ArmorTrim(tm, tp);
                     am.setTrim(trim);
                  }
               } catch (Exception var23) {
                  this.plugin.getLogger().warning("Invalid trim in item-editor '" + crateName + "/" + itemKey + "': " + var23.getMessage());
               }
               bim = am;
            }
         }

         base.setItemMeta(bim);
      }

      Inventory inv = Bukkit.createInventory((InventoryHolder) null, 27, Utils.formatColors(crateName + " Item Editor"));
      inv.setItem(10, base);

      ItemStack nameBtn = new ItemStack(Material.NAME_TAG);
      ItemMeta nm = nameBtn.getItemMeta();
      if (nm != null) {
         nm.setDisplayName(Utils.formatColors("&dEdit Display Name"));
         nm.setLore(Utils.formatColors(List.of("&7Click to edit name in chat")));
         nameBtn.setItemMeta(nm);
      }
      inv.setItem(13, nameBtn);

      ItemStack loreBtn = new ItemStack(Material.BOOK);
      ItemMeta lm = loreBtn.getItemMeta();
      if (lm != null) {
         lm.setDisplayName(Utils.formatColors("&bEdit Item Lore"));
         lm.setLore(Utils.formatColors(List.of("&7Left-click to add a lore line", "&7Shift+Right-click to clear all lore")));
         loreBtn.setItemMeta(lm);
      }
      inv.setItem(14, loreBtn);

      boolean give = it.getBoolean("giveitem", true);
      ItemStack tog = new ItemStack(give ? Material.GREEN_WOOL : Material.RED_WOOL);
      ItemMeta tm = tog.getItemMeta();
      if (tm != null) {
         tm.setDisplayName(Utils.formatColors("&aGive Item: " + give));
         tm.setLore(Utils.formatColors(List.of("&7Click to toggle giveitem")));
         tog.setItemMeta(tm);
      }
      inv.setItem(15, tog);

      String cmd = it.getString("command", "");
      ItemStack cmdItem = new ItemStack(Material.PAPER);
      ItemMeta cm = cmdItem.getItemMeta();
      if (cm != null) {
         cm.setDisplayName(Utils.formatColors("&aSet Reward Command"));
         cm.setLore(Utils.formatColors(List.of("&7Click then enter command in chat", "&7Current: " + (cmd.isEmpty() ? "<none>" : cmd))));
         cmdItem.setItemMeta(cm);
      }
      inv.setItem(16, cmdItem);

      ItemStack back = new ItemStack(Material.ARROW);
      ItemMeta bm2 = back.getItemMeta();
      if (bm2 != null) {
         bm2.setDisplayName(Utils.formatColors("&eBack to Rewards"));
         back.setItemMeta(bm2);
      }
      inv.setItem(18, back);

      ItemStack del = new ItemStack(Material.BARRIER);
      ItemMeta dm = del.getItemMeta();
      if (dm != null) {
         dm.setDisplayName(Utils.formatColors("&cDelete This Item"));
         dm.setLore(Utils.formatColors(List.of("&7Click to remove this reward")));
         del.setItemMeta(dm);
      }
      inv.setItem(26, del);
      return inv;
   }
}
