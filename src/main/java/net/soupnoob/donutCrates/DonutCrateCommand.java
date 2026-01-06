package net.soupnoob.donutCrates;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class DonutCrateCommand implements CommandExecutor, TabCompleter {
   private final DonutCrates plugin;

   public DonutCrateCommand(DonutCrates plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("donutcrates.admin")) {
         String noPerm = Utils.formatColors(this.plugin.cfg.config.getString("no-permission"));
         if (sender instanceof Player) {
            this.sendBoth((Player) sender, noPerm);
         } else {
            sender.sendMessage(noPerm);
         }
         return true;
      }

      if (args.length < 1) {
         this.sendUsage(sender, "help");
         return true;
      }

      String sub = args[0].toLowerCase(Locale.ROOT);

      switch (sub) {
         case "create": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            if (args.length != 2) {
               this.sendUsage(sender, "create");
               return true;
            }

            Player target = (Player) sender;
            String name = args[1];
            if (this.plugin.crateMgr.crateExists(name)) {
               this.sendBoth(target, Utils.formatColors("&cA crate named &e" + name + " &calready exists."));
               return true;
            }

            Block newLoc = target.getTargetBlockExact(5);
            if (newLoc == null) {
               this.sendBoth(target, Utils.formatColors("&cYou must be looking at a block."));
               return true;
            }

            this.plugin.crateMgr.createCrate(name, newLoc, target);
            return true;
         }
         case "delete": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            if (args.length != 2) {
               this.sendUsage(sender, "delete");
               return true;
            }

            Player target = (Player) sender;
            String name = args[1];
            if (!this.plugin.crateMgr.crateExists(name)) {
               this.sendBoth(target, Utils.formatColors("&cNo crate named &e" + name + " &cexists."));
               return true;
            }

            this.plugin.crateMgr.deleteCrate(name, target);
            return true;
         }
         case "moveblock": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            if (args.length != 2) {
               this.sendUsage(sender, "moveblock");
               return true;
            }

            Player target = (Player) sender;
            String name = args[1];
            if (!this.plugin.crateMgr.crateExists(name)) {
               this.sendBoth(target, Utils.formatColors("&cNo crate named &e" + name + " &cexists."));
               return true;
            }

            Block newLoc = target.getTargetBlockExact(5);
            if (newLoc == null) {
               this.sendBoth(target, Utils.formatColors("&cYou must be looking at a block."));
               return true;
            }

            this.plugin.crateMgr.moveCrate(name, newLoc, target);
            return true;
         }
         case "removeitem": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            if (args.length != 3) {
               this.sendUsage(sender, "removeitem");
               return true;
            }

            Player target = (Player) sender;
            String crate = args[1];
            String key = args[2];
            if (!this.plugin.crateMgr.crateExists(crate)) {
               this.sendBoth(target, Utils.formatColors("&cNo crate named &e" + crate + " &cexists."));
               return true;
            }

            this.plugin.crateMgr.removeItem(crate, key, target);
            return true;
         }
         case "addhanditem": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            if (args.length != 2) {
               this.sendUsage(sender, "addhanditem");
               return true;
            }

            Player target = (Player) sender;
            String crate = args[1];
            if (!this.plugin.crateMgr.crateExists(crate)) {
               this.sendBoth(target, Utils.formatColors("&cNo crate named &e" + crate + " &cexists."));
               return true;
            }

            this.plugin.crateMgr.addHandItem(crate, target);
            return true;
         }
         case "keygive": {
            if (args.length != 4) {
               this.sendUsage(sender, "keygive");
               return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage(Utils.formatColors("&cPlayer not found."));
               return true;
            }

            String crate = args[2];
            if (!this.plugin.crateMgr.crateExists(crate)) {
               sender.sendMessage(Utils.formatColors("&cNo crate named &e" + crate + " &cexists."));
               return true;
            }

            int amount;
            try {
               amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
               this.sendUsage(sender, "keygive");
               return true;
            }

            this.plugin.dataMgr.modifyKeys(target, crate, amount);
            String tpl = this.plugin.cfg.config.getString("messages.keyreceive", "&aYou received %amount% %crate% keys!");
            String formattedRecipient = Utils.formatColors(tpl.replace("%amount%", String.valueOf(amount)).replace("%crate%", crate));
            this.sendBoth(target, formattedRecipient);
            String adminMsg = Utils.formatColors("&aGave &e" + amount + " &akeys of &e" + crate);
            if (sender instanceof Player) {
               this.sendBoth((Player) sender, adminMsg);
            } else {
               sender.sendMessage(adminMsg);
            }
            return true;
         }
         case "removekey": {
            if (args.length != 4) {
               this.sendUsage(sender, "removekey");
               return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               return true;
            }

            String crate = args[2];
            if (!this.plugin.crateMgr.crateExists(crate)) return true;

            int amount;
            try {
               amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
               this.sendUsage(sender, "removekey");
               return true;
            }

            this.plugin.dataMgr.modifyKeys(target, crate, -amount);
            return true;
         }
         case "keygiveall": {
            if (args.length != 3) {
               this.sendUsage(sender, "keygiveall");
               return true;
            }
            String crate = args[1];
            if (!this.plugin.crateMgr.crateExists(crate)) {
               sender.sendMessage(Utils.formatColors("&cNo crate named &e" + crate + " &cexists."));
               return true;
            }

            int amount;
            try {
               amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
               this.sendUsage(sender, "keygiveall");
               return true;
            }

            String tpl = this.plugin.cfg.config.getString("messages.keyreceive", "&aYou received %amount% %crate% keys!");
            for (Player pl : Bukkit.getOnlinePlayers()) {
               this.plugin.dataMgr.modifyKeys(pl, crate, amount);
               String msg = Utils.formatColors(tpl.replace("%amount%", String.valueOf(amount)).replace("%crate%", crate));
               this.sendBoth(pl, msg);
            }

            String summary = Utils.formatColors("&aGave &e" + amount + " &akeys of &e" + crate + " to all players.");
            if (sender instanceof Player) {
               this.sendBoth((Player) sender, summary);
            } else {
               sender.sendMessage(summary);
            }
            return true;
         }
         case "editor": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            Player target = (Player) sender;
            target.openInventory(this.plugin.guiMgr.buildMainEditorGUI());
            return true;
         }
         case "reload": {
            if (!(sender instanceof Player)) {
               sender.sendMessage("Only players may use this command.");
               return true;
            }
            Player target = (Player) sender;
            this.plugin.cfg.reloadAll();
            this.plugin.crateMgr.saveBlocks();
            this.plugin.dataMgr.saveAll();
            this.sendBoth(target, Utils.formatColors("&aReloaded configuration and data."));
            return true;
         }
         case "list": {
            Set<String> crateNames = this.plugin.crateMgr.crateBlocks.keySet();
            if (crateNames.isEmpty()) {
               sender.sendMessage(Utils.formatColors("&cNo crates have been created yet."));
               return true;
            }
            sender.sendMessage(Utils.formatColors("&aCrates list:"));
            for (String s : crateNames) {
               sender.sendMessage(Utils.formatColors("&e" + s));
            }
            return true;
         }
         default:
            this.sendUsage(sender, "help");
            return true;
      }
   }

   @Override
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> subs = List.of("create", "delete", "moveblock", "removeitem", "addhanditem", "keygive", "keygiveall", "removekey", "editor", "reload", "list");
      if (args.length == 1) {
         return StringUtil.copyPartialMatches(args[0], subs, new ArrayList<>());
      }

      String sub = args[0].toLowerCase(Locale.ROOT);
      if ("keygive".equals(sub)) {
         if (args.length == 2) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());
         } else if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], new ArrayList<>(this.plugin.crateMgr.crateBlocks.keySet()), new ArrayList<>());
         }
      } else if ("removekey".equals(sub)) {
         if (args.length == 2) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());
         } else if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], new ArrayList<>(this.plugin.crateMgr.crateBlocks.keySet()), new ArrayList<>());
         }
      } else if (("delete".equals(sub) || "moveblock".equals(sub) || "removeitem".equals(sub) || "addhanditem".equals(sub) || "keygiveall".equals(sub)) && args.length == 2) {
         return StringUtil.copyPartialMatches(args[1], new ArrayList<>(this.plugin.crateMgr.crateBlocks.keySet()), new ArrayList<>());
      }

      return List.of();
   }

   private void sendBoth(Player player, String message) {
      player.sendMessage(message);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
   }

   private void sendUsage(CommandSender sender, String sub) {
      String usage;
      switch (sub) {
         case "create": usage = "/donutcrate create <name>"; break;
         case "delete": usage = "/donutcrate delete <name>"; break;
         case "moveblock": usage = "/donutcrate moveblock <name>"; break;
         case "removeitem": usage = "/donutcrate removeitem <crate> <itemKey>"; break;
         case "addhanditem": usage = "/donutcrate addhanditem <crate>"; break;
         case "keygive": usage = "/donutcrate keygive <player> <crate> <amount>"; break;
         case "removekey": usage = "/donutcrate removekey <player> <crate> <amount>"; break;
         case "keygiveall": usage = "/donutcrate keygiveall <crate> <amount>"; break;
         case "editor": usage = "/donutcrate editor"; break;
         case "reload": usage = "/donutcrate reload"; break;
         case "list": usage = "/donutcrate list"; break;
         default:
            usage = "/donutcrate <create|delete|moveblock|removeitem|addhanditem|keygive|removekey|keygiveall|editor|reload|list>";
      }
      String prefix = this.plugin.cfg.config.getString("messages.usage", "&cUsage: &#91b8ff");
      String msg = Utils.formatColors(prefix + usage);
      if (sender instanceof Player) {
         ((Player) sender).sendMessage(msg); // nur Chat, keine Action-Bar
      } else {
         sender.sendMessage(msg);
      }
   }
}
