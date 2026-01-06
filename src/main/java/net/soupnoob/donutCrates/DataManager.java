package net.soupnoob.donutCrates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class DataManager {
   private final DonutCrates plugin;
   public final Map<String, Map<String, Integer>> playerKeys = new HashMap<>();

   public DataManager(DonutCrates pl) {
      this.plugin = pl;
      ConfigurationSection ps = this.plugin.cfg.saves.getConfigurationSection("playerKeys");
      if (ps != null) {
         Iterator<String> it = ps.getKeys(false).iterator();

         while (it.hasNext()) {
            String uuid = it.next();
            ConfigurationSection sub = ps.getConfigurationSection(uuid);
            Map<String, Integer> map = new HashMap<>();
            if (sub != null) {
               for (String k : sub.getKeys(false)) {
                  map.put(k, sub.getInt(k));
               }
            }
            this.playerKeys.put(uuid, map);
         }
      }

   }

   public void saveAll() {
      ConfigurationSection ps = this.plugin.cfg.saves.createSection("playerKeys");
      for (Entry<String, Map<String, Integer>> e : this.playerKeys.entrySet()) {
         ConfigurationSection sub = ps.createSection(e.getKey());
         Map<String, Integer> map = e.getValue();
         if (map != null) {
            map.forEach(sub::set);
         }
      }

      this.plugin.cfg.saveAll();
   }

   public int getKeys(Player p, String crate) {
      String uid = p.getUniqueId().toString();
      Map<String, Integer> map = this.playerKeys.computeIfAbsent(uid, (k) -> new HashMap<>());
      int count = 0;
      if (map.containsKey(crate + "_Key")) {
         count = map.get(crate + "_Key");
      } else if (map.containsKey(crate)) {
         count = map.get(crate);
      }

      return count;
   }

   public void modifyKeys(Player p, String crate, int delta) {
      String uid = p.getUniqueId().toString();
      Map<String, Integer> map = this.playerKeys.computeIfAbsent(uid, (k) -> new HashMap<>());
      String keyName = crate + "_Key";
      int newVal = map.getOrDefault(keyName, 0) + delta;
      map.put(keyName, newVal);
      this.plugin.cfg.saveAll();
   }
}
