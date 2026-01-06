package net.soupnoob.donutCrates;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class BlockListener implements Listener {
    private final DonutCrates plugin;

    public BlockListener(DonutCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block b = e.getClickedBlock();
        if (b == null) return;
        if (!plugin.crateMgr.isCrateBlock(b)) return;

        e.setCancelled(true);

        String name = plugin.crateMgr.getCrateByBlock(b);
        if (name == null) return;

        Player p = e.getPlayer();
        Inventory inv = plugin.guiMgr.buildCrateGUI(name);
        if (inv == null) return;

        p.openInventory(inv);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.crateMgr.isCrateBlock(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.crateMgr.isCrateBlock(e.getBlockPlaced())) {
            e.setCancelled(true);
        }
    }
}
