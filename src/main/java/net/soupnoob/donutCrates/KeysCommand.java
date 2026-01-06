package net.soupnoob.donutCrates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class KeysCommand implements CommandExecutor, TabCompleter {
    private final DonutCrates plugin;

    public KeysCommand(DonutCrates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /keys  -> eigene Keys (nur Player), benötigt permission donutcrates.keys
        // /keys <player> -> Keys des Spielers, benötigt permission donutcrates.otherkeys
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.formatColors("&cDieser Befehl muss von einem Spieler ausgeführt werden."));
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("donutcrates.keys")) {
                sender.sendMessage(Utils.formatColors(this.plugin.cfg.config.getString("no-permission", "&cKeine Berechtigung")));
                return true;
            }
            sendKeysList(p, p);
            return true;
        } else {
            // Zielspieler prüfen
            if (!sender.hasPermission("donutcrates.otherkeys")) {
                sender.sendMessage(Utils.formatColors(this.plugin.cfg.config.getString("no-permission", "&cKeine Berechtigung")));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Utils.formatColors("&cSpieler nicht gefunden."));
                return true;
            }
            sendKeysList(sender, target);
            return true;
        }
    }

    private void sendKeysList(CommandSender receiver, Player subject) {
        Set<String> crates = this.plugin.crateMgr == null ? Set.of() : this.plugin.crateMgr.crateBlocks.keySet();
        if (crates.isEmpty()) {
            receiver.sendMessage(Utils.formatColors("&cEs wurden noch keine Crates erstellt."));
            return;
        }
        receiver.sendMessage(Utils.formatColors("&6Keys für &e" + subject.getName() + "&6:"));
        for (String crate : crates) {
            int count = this.plugin.dataMgr.getKeys(subject, crate);
            receiver.sendMessage(Utils.formatColors("&e" + crate + ": &a" + count));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[0], players, new ArrayList<>());
        }
        return List.of();
    }
}
