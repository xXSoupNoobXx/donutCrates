package net.soupnoob.donutCrates;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CrateHolder implements InventoryHolder {
   private final String crateName;
   private Inventory inventory;

   public CrateHolder(String crateName) {
      this.crateName = crateName;
   }

   public String getCrateName() {
      return this.crateName;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }
}
