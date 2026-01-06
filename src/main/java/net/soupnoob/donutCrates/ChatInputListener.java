package net.soupnoob.donutCrates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

public class ChatInputListener implements Listener {
   private final DonutCrates plugin;

   public ChatInputListener(DonutCrates plugin) {
      this.plugin = plugin;
   }

   @EventHandler(ignoreCancelled = true)
   public void onPlayerChat(AsyncPlayerChatEvent e) {
      final Player p = e.getPlayer();
      final UUID uid = p.getUniqueId();

      if (!this.plugin.pendingEditorCrate.containsKey(uid) || !this.plugin.pendingEditorItemKey.containsKey(uid)) {
         return;
      }

      e.setCancelled(true);
      final String crate = this.plugin.pendingEditorCrate.get(uid);
      final String key = this.plugin.pendingEditorItemKey.get(uid);
      final String msg = e.getMessage().trim();

      // Verschiebe alle Bukkit-/Config‑Operationen auf den Hauptthread
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
         // Sicherstellen, dass der Eintrag noch existiert
         if (!this.plugin.pendingEditorCrate.containsKey(uid) || !this.plugin.pendingEditorItemKey.containsKey(uid)) {
            return;
         }

         Boolean renameFlag = this.plugin.pendingEditorIsRename.remove(uid);
         Boolean loreFlag = this.plugin.pendingEditorIsLore.remove(uid);

         if (renameFlag != null && renameFlag) {
            if (this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items." + key) != null) {
               this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items." + key).set("displayname", msg);
               this.plugin.cfg.saveAll();
               p.sendMessage(Utils.formatColors("&aSet display name to: &f" + msg));
            }
         } else if (loreFlag != null && loreFlag) {
            List<String> lore = new ArrayList<>(this.plugin.cfg.crates.getStringList("Crates." + crate + ".Items." + key + ".lore"));
            lore.add(msg);
            this.plugin.cfg.crates.set("Crates." + crate + ".Items." + key + ".lore", lore);
            this.plugin.cfg.saveAll();
            p.sendMessage(Utils.formatColors("&aAdded lore: &f" + msg));
         } else {
            if (this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items." + key) != null) {
               this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items." + key).set("command", msg);
               this.plugin.cfg.saveAll();
               p.sendMessage(Utils.formatColors("&aSet command: &f" + msg));
            }
         }

         Inventory inv = this.plugin.guiMgr.buildEditorItemMenu(crate, key);
         if (inv != null) {
            p.openInventory(inv);
         }
      });
   }
}
