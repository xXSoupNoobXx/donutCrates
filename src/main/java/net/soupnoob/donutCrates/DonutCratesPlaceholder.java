package net.soupnoob.donutCrates;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DonutCratesPlaceholder extends PlaceholderExpansion {
   @Override
   public String getIdentifier() {
      return "donutcrates";
   }

   @Override
   public String getAuthor() {
      return "DonutCratesPlugin";
   }

   @Override
   public String getVersion() {
      return DonutCrates.instance != null && DonutCrates.instance.getDescription() != null
              ? DonutCrates.instance.getDescription().getVersion()
              : "unknown";
   }

   @Override
   public boolean persist() {
      return true;
   }

   @Override
   public String onRequest(OfflinePlayer player, String params) {
      if (params == null) return null;

      if (params.startsWith("key_")) {
         String crate = params.substring(4);
         Player online = player != null ? player.getPlayer() : null;
         int keys = 0;
         if (online != null && DonutCrates.instance != null && DonutCrates.instance.dataMgr != null) {
            keys = DonutCrates.instance.dataMgr.getKeys(online, crate);
         }
         return String.valueOf(keys);
      }

      return null;
   }
}
