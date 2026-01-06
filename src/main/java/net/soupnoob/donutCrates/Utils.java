package net.soupnoob.donutCrates;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

public final class Utils {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

   private Utils() {
   }

   public static String formatColors(String input) {
      if (input == null) {
         return null;
      } else {
         Matcher matcher = HEX_PATTERN.matcher(input);
         StringBuffer buffer = new StringBuffer(input.length() + 32);

         while(matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder repl = new StringBuilder("§x");
            char[] var5 = hex.toCharArray();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               char c = var5[var7];
               repl.append('§').append(c);
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(repl.toString()));
         }

         matcher.appendTail(buffer);
         return ChatColor.translateAlternateColorCodes('&', buffer.toString());
      }
   }

   public static String stripColor(String s) {
      return ChatColor.stripColor(s);
   }

   public static List<String> formatColors(List<String> lines) {
      return (List)lines.stream().map(Utils::formatColors).collect(Collectors.toList());
   }
}
