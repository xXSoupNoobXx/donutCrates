package net.soupnoob.donutCrates;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
   private final DonutCrates plugin;

   public InventoryClickListener(DonutCrates pl) {
      this.plugin = pl;
   }

   @EventHandler
   public void onInvClick(InventoryClickEvent e) {
      HumanEntity who = e.getWhoClicked();
      if (!(who instanceof Player)) return;
      Player p = (Player) who;

      // Direkter API-Zugriff statt Reflection
      InventoryView view = e.getView();
      String title = view.getTitle();

      ClickType ct = e.getClick();

      InventoryHolder holder = view.getTopInventory() != null ? view.getTopInventory().getHolder() : null;

      if (holder instanceof CrateHolder) {
         CrateHolder ch = (CrateHolder) holder;
         int topSize = ch.getInventory().getSize();
         if (e.getRawSlot() < topSize) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String crateKey = ch.getCrateName();
            ConfigurationSection crateCfg = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateKey);
            if (crateCfg == null) return;

            boolean fillerEnabled = crateCfg.getBoolean("fillerEnabled", false);
            String fillerMat = crateCfg.getString("fillerMaterial", "");
            if (fillerEnabled && clicked.getType().name().equalsIgnoreCase(fillerMat)) return;

            int keyCount = this.plugin.dataMgr.getKeys(p, crateKey);

            if (keyCount <= 0) {
               Sound noKeySound = parseSound(this.plugin.cfg.config.getString("sounds.no-key", "ENTITY_VILLAGER_NO"));
               if (noKeySound != null) p.playSound(p.getLocation(), noKeySound, 1.0F, 1.0F);
            } else {
               Sound clickSound = parseSound(this.plugin.cfg.config.getString("sounds.click", "UI_BUTTON_CLICK"));
               if (clickSound != null) p.playSound(p.getLocation(), clickSound, 1.0F, 1.0F);

               this.plugin.pendingCrate.put(p.getUniqueId(), crateKey);
               this.plugin.pendingSlot.put(p.getUniqueId(), e.getRawSlot());
               p.openInventory(this.plugin.guiMgr.buildConfirmGUI(p, clicked, crateKey));
            }
         }
         return;
      }

      ConfigurationSection confirmCfg = this.plugin.cfg.config.getConfigurationSection("confirm-menu");
      if (confirmCfg != null && title.equals(Utils.formatColors(confirmCfg.getString("title")))) {
         e.setCancelled(true);
         ItemStack clicked = e.getCurrentItem();
         if (clicked == null || clicked.getType() == Material.AIR) return;

         UUID uid = p.getUniqueId();
         String crateKey = this.plugin.pendingCrate.remove(uid);
         Integer slot = this.plugin.pendingSlot.remove(uid);
         if (crateKey == null || slot == null) {
            p.closeInventory();
            return;
         }

         if (ct.isLeftClick() && clicked.getType().name().equalsIgnoreCase(confirmCfg.getString("Confirm.material"))) {
            if (this.plugin.dataMgr.getKeys(p, crateKey) > 0) {
               this.plugin.dataMgr.modifyKeys(p, crateKey, -1);
               ItemStack reward = e.getInventory().getItem(confirmCfg.getInt("ClickedItem.slot"));
               ConfigurationSection rewardsSection = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateKey + ".Items");
               if (rewardsSection != null) {
                  for (String rewardKey : rewardsSection.getKeys(false)) {
                     ConfigurationSection it = rewardsSection.getConfigurationSection(rewardKey);
                     if (it == null) continue;
                     if (it.getInt("slot") == slot) {
                        if (it.getBoolean("giveitem", true) && reward != null) {
                           p.getInventory().addItem(reward.clone());
                        }
                        String cmd = it.getString("command", "");
                        if (cmd != null && !cmd.isBlank()) {
                           Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));
                        }
                        break;
                     }
                  }
               }

               Sound claimSound = parseSound(this.plugin.cfg.config.getString("sounds.claim", "ENTITY_PLAYER_LEVELUP"));
               if (claimSound != null) p.playSound(p.getLocation(), claimSound, 1.0F, 1.0F);
            } else {
               p.sendMessage(Utils.formatColors(this.plugin.cfg.config.getString("no-keys-msg", "&cYou have no keys")).replace("%crate%", crateKey));
            }
         }
         p.closeInventory();
         return;
      }

      if (title.equals(Utils.formatColors("&6Crate Editor"))) {
         e.setCancelled(true);
         ItemStack clicked = e.getCurrentItem();
         if (clicked == null || clicked.getType() == Material.AIR) return;

         String crate = Utils.stripColor(clicked.getItemMeta().getDisplayName());
         if (ct == ClickType.SHIFT_RIGHT) {
            this.plugin.crateMgr.deleteCrate(crate, p);
            p.openInventory(this.plugin.guiMgr.buildMainEditorGUI());
         } else if (ct.isLeftClick()) {
            p.openInventory(this.plugin.guiMgr.buildCrateEditorGUI(crate));
         }
         return;
      }

      if (title.endsWith(" Editor") && !title.endsWith(" Item Editor")) {
         e.setCancelled(true);
         String crate = title.replace(" Editor", "");
         int slot = e.getRawSlot();
         if (ct == ClickType.SHIFT_LEFT) {
            int target = slot - 1;
            if (target >= 0) swapSlots(crate, slot, target);
            p.openInventory(this.plugin.guiMgr.buildCrateEditorGUI(crate));
            return;
         } else if (ct == ClickType.SHIFT_RIGHT) {
            int invSize = e.getInventory().getSize();
            int target = slot + 1;
            if (target < invSize) swapSlots(crate, slot, target);
            p.openInventory(this.plugin.guiMgr.buildCrateEditorGUI(crate));
            return;
         } else {
            ConfigurationSection itemsSec = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
            if (itemsSec != null) {
               String foundKey = null;
               for (String itemKey : itemsSec.getKeys(false)) {
                  ConfigurationSection it = itemsSec.getConfigurationSection(itemKey);
                  if (it != null && it.getInt("slot") == slot) {
                     foundKey = itemKey;
                     break;
                  }
               }

               if (slot == 45) {
                  p.openInventory(this.plugin.guiMgr.buildMainEditorGUI());
               } else if (foundKey != null && ct.isLeftClick()) {
                  this.plugin.pendingEditorCrate.put(p.getUniqueId(), crate);
                  this.plugin.pendingEditorItemKey.put(p.getUniqueId(), foundKey);
                  p.openInventory(this.plugin.guiMgr.buildEditorItemMenu(crate, foundKey));
               }
            }
         }
         return;
      }

      if (title.endsWith(" Item Editor")) {
         e.setCancelled(true);
         String crate = title.replace(" Item Editor", "");
         UUID uid = p.getUniqueId();
         String itemKey = this.plugin.pendingEditorItemKey.get(uid);
         if (itemKey == null) return;

         if (e.getRawSlot() == 26) {
            ConfigurationSection items = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
            if (items != null) {
               items.set(itemKey, null);
               this.plugin.cfg.saveAll();
               p.sendMessage(Utils.formatColors("&cRemoved reward &e" + itemKey));
            }
            this.plugin.pendingEditorCrate.remove(uid);
            this.plugin.pendingEditorItemKey.remove(uid);
            this.plugin.pendingEditorIsLore.remove(uid);
            this.plugin.pendingEditorIsRename.remove(uid);
            p.openInventory(this.plugin.guiMgr.buildCrateEditorGUI(crate));
            return;
         }

         if (e.getRawSlot() == 18) {
            this.plugin.pendingEditorCrate.remove(uid);
            this.plugin.pendingEditorItemKey.remove(uid);
            this.plugin.pendingEditorIsLore.remove(uid);
            this.plugin.pendingEditorIsRename.remove(uid);
            p.openInventory(this.plugin.guiMgr.buildCrateEditorGUI(crate));
            return;
         }

         if (e.getRawSlot() == 13) {
            this.plugin.pendingEditorCrate.put(uid, crate);
            this.plugin.pendingEditorItemKey.put(uid, itemKey);
            this.plugin.pendingEditorIsRename.put(uid, true);
            p.closeInventory();
            p.sendMessage(Utils.formatColors("&eEnter new display name for this item:"));
            return;
         }

         if (e.getRawSlot() == 14) {
            if (ct == ClickType.SHIFT_RIGHT) {
               this.plugin.cfg.crates.set("Crates." + crate + ".Items." + itemKey + ".lore", new ArrayList<>());
               this.plugin.cfg.saveAll();
               p.sendMessage(Utils.formatColors("&aCleared all lore for " + itemKey));
               p.openInventory(this.plugin.guiMgr.buildEditorItemMenu(crate, itemKey));
            } else {
               this.plugin.pendingEditorCrate.put(uid, crate);
               this.plugin.pendingEditorItemKey.put(uid, itemKey);
               this.plugin.pendingEditorIsLore.put(uid, true);
               p.closeInventory();
               p.sendMessage(Utils.formatColors("&eEnter a lore line for this item:"));
            }
            return;
         }

         if (e.getRawSlot() == 15) {
            boolean give = this.plugin.cfg.crates.getBoolean("Crates." + crate + ".Items." + itemKey + ".giveitem", true);
            this.plugin.cfg.crates.set("Crates." + crate + ".Items." + itemKey + ".giveitem", !give);
            this.plugin.cfg.saveAll();
            p.openInventory(this.plugin.guiMgr.buildEditorItemMenu(crate, itemKey));
            return;
         }

         if (e.getRawSlot() == 16) {
            this.plugin.pendingEditorCrate.put(uid, crate);
            this.plugin.pendingEditorItemKey.put(uid, itemKey);
            this.plugin.pendingEditorIsLore.put(uid, false);
            p.closeInventory();
            p.sendMessage(Utils.formatColors("&eEnter a command to run when this reward is chosen:"));
            return;
         }
      }
   }

   private void swapSlots(String crate, int slotA, int slotB) {
      ConfigurationSection items = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
      if (items == null) return;

      String keyA = null;
      String keyB = null;
      for (String k : items.getKeys(false)) {
         ConfigurationSection it = items.getConfigurationSection(k);
         if (it == null) continue;
         int s = it.getInt("slot");
         if (s == slotA) keyA = k;
         if (s == slotB) keyB = k;
      }

      if (keyA != null) items.getConfigurationSection(keyA).set("slot", slotB);
      if (keyB != null) items.getConfigurationSection(keyB).set("slot", slotA);
      this.plugin.cfg.saveAll();
   }

   @SuppressWarnings("deprecation")
   private Sound parseSound(String name) {
      if (name == null || name.isBlank()) return null;
      try {
         return Sound.valueOf(name);
      } catch (IllegalArgumentException ex) {
         // fallback: try uppercase trimmed
         try {
            return Sound.valueOf(name.trim().toUpperCase());
         } catch (IllegalArgumentException ex2) {
            return null;
         }
      }
   }
}
