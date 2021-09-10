package net.thearchon.hq.language;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ChatColor;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.handler.factions.FactionsClient;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Message {

    PREFIX("&c&l&oTheArchon&r "),
    STAFF_PREFIX("&7[&c&lSTAFF&7]&r "),
    WARNING_PREFIX("&c&l[WARNING]&r "),
    CONSOLE("&c[Console]&r"),
    UNKNOWN_COMMAND("Unknown command. Type /help for help."),
    COMMAND_DISABLED("&cThis command is disabled."),
    QUEUE_PREFIX("&7&l<&a&lQUEUE&7&l> &r"),

    RECORD("&aWe just hit a record of &b%s &aplayers online!"),

    SEARCHING("&7Searching, please wait..."),

    CONNECTING_TO_SERVER("&aConnecting you to &e%s&a, please wait..."),
    TO_AVAILABLE_LOBBY("&cConnecting you to an available lobby..."),
    SERVER_OFFLINE("&cServer %s is currently offline."),
    SERVER_FULL("&c%s is currently full. Please try again later."),
    SERVER_UNAVAILABLE("&cCannot connect to %s at this time."),
    SERVER_NOT_FOUND("&cServer not found: %s"),

    CHARGEBACK_BAN("Banned for charging back on shop.thearchon.net"),

    NO_AVAILABLE_LOBBIES("&cThere are no available lobbies to connect to. Try reconnecting in a few seconds."),
    SERVER_RESTARTING("&cThis server is currently restarting. Please try again in a few seconds."),
    PLAYER_NOT_FOUND("&cPlayer not found: %s"),

    VPN_BLOCKED("&cYou're not allowed to connect using a VPN or proxy, please switch it off."),

    UNBANNED_ALL_PLAYERS("{p}Unbanned all players! {s}(%d total)"),

    ;

    private String message;

    Message(String message) {
        this.message = message;
    }

    public void set(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(toString(), args);
    }

    public String replace(String tag, String text) {
        return toString().replace('{' + tag + '}', text);
    }

    public String error() {
        return ERROR_COLOR + toString();
    }

    @Override
    public String toString() {
        return message
                .replace("{p}", PRIMARY_COLOR.toString())
                .replace("{s}", SECONDARY_COLOR.toString())
                .replace("{e}", ERROR_COLOR.toString());
    }

    public static final String NAME = "TheArchon";

    public static final ChatColor PRIMARY_COLOR = ChatColor.GRAY;
    public static final ChatColor SECONDARY_COLOR = ChatColor.GOLD;
    public static final ChatColor ERROR_COLOR = ChatColor.RED;

    public static final char BOX = '█';
    public static final char CHECK = '✔';
    public static final char LEADING_ARROW = '»';
    public static final char TRAILING_ARROW = '«';
    public static final String BAR = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

    public static void set(Map<String, String> messages) {
        for (Entry<String, String> entry : messages.entrySet()) {
            Message message = valueOf(convert(entry.getKey()));
            if (message != null) {
                message.message = entry.getValue();
            }
        }
    }

    public static Map<String, String> defaults() {
        Message[] values = values();
        Map<String, String> defaults = new LinkedHashMap<>(values.length);
        for (Message message : values) {
            defaults.put(message.name(), message.message);
        }
        return defaults;
    }

    /**
     * Replaces lower camel case text to upper underscore text.
     * e.g., myText -> MY_TEXT
     * @param input text to convert
     * @return converted text
     */
    public static String convert(String input) {
        return input.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
    }

    public static void notifyServerStatus(Archon archon, BukkitClient client, boolean online) {
        String disp = client.getServerName();
        if (client.getType() == ServerType.FACTIONS) {
            disp = ((FactionsClient) client).getColor() + disp;
        }
        if (online) {
            archon.notifyStaff(
                    "&a" + Message.BAR,
                    "&7[&c&lArchon&7] &a&lStatus &8&l> &a&o" + disp + " &7(&a" + client.getOnlineCount() + "&7/&a" + client.getSlots() + "&7) &7connected",
                    "&a" + Message.BAR);
        } else {
            archon.notifyStaff(
                    "&4" + Message.BAR,
                    "&7[&c&lArchon&7] &c&lStatus &8&l> &c&o" + disp + " &7disconnected",
                    "&4" + Message.BAR);
        }
    }

//    private static final Pattern ipPattern = Pattern.compile("((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[.,-:; ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9]))");
//    private static final Pattern webPattern = Pattern.compile("[-a-zA-Z0-9@:%_\\+.~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+~#?&//=]*)?");

    private static final Pattern ipPattern = Pattern.compile("\\b[0-9]{1,3}(\\.|dot|\\(dot\\)|-|;|:|,|(\\W|\\d|_)*\\s)+[0-9]{1,3}(\\.|dot|\\(dot\\)|-|;|:|,|(\\W|\\d|_)*\\s)+[0-9]{1,3}(\\.|dot|\\(dot\\)|-|;|:|,|(\\W|\\d|_)*\\s)+[0-9]{1,3}\\b");
    private static final Pattern webPattern = Pattern.compile("[a-zA-Z0-9\\-\\.]+\\s?(\\.|dot|\\(dot\\)|-|;|:|,)\\s?(com|org|net|cz|co|uk|sk|biz|mobi|xxx|eu|me|io)\\b");

    public static void checkMessage(Archon archon, Player player, String message) {
        if (player.isStaff()) return;
        if (message.toLowerCase().contains("archon")) return;
        if (message.toLowerCase().startsWith("//")) return;

        List<String> pos = new ArrayList<>();
        message = Normalizer.normalize(message, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        if (message.contains("/pay") || message.contains("/auc")) {
            return;
        }
        Matcher m = ipPattern.matcher(message);
        while (m.find()) {
            String s = m.group().trim();
            if (s.length() != 0 && ipPattern.matcher(message).find()) {
                if (s.contains("..") || s.contains("...")) {
                    continue;
                }
                pos.add(s);
            }
        }
        m = webPattern.matcher(message);
        while (m.find()) {
            String s = m.group().trim();
            if (s.length() != 0 && webPattern.matcher(message).find()) {
                if (s.contains("..") || s.contains("...")) {
                    continue;
                }
                pos.add(s);
            }
        }
        if (!pos.isEmpty()) {
            String msg = message;
            for (String p : pos) {
                msg = msg.replace(p, " &d" + p + "&a");
            }
            archon.notifyStaff("&cPossible advertising from " + player.getDisplayName() + " &3(" + player.getCurrentServer().getServerName() + ") &a" + msg);
            return;
        }
        int capCount = uppercaseCount(message);
        if (capCount >= 20) {
            //archon.notifyStaff(archon.getStaffPrefix() + "&4Warning: &cExcessive caps (" + capCount + ") from [&7" + player.getDisplayName() + "&c] &3(" + player.getCurrentServer() + ") &a" + message);
            player.setCapWarns(player.getCapWarns() + 1);
            if (player.getCapWarns() >= 3) {
                player.setCapWarns(0);
                archon.notifyStaff(player.getDisplayName() + " &7has been kicked for using too many caps &c3/3 &7times.");
                player.disconnect("&cYou have been kicked for using an excessive amount of caps after being warned 3 times!");
                return;
            }
            player.message(
                    "&4" + Message.BAR,
                    "&c&lPlease stop using too many capital letters &a&l(" + capCount + ")",
                    "&6You have &c&l" + (3 - player.getCapWarns()) + " &6" + (((3 - player.getCapWarns()) == 1) ? "warning" : "warnings") + " left before you get kicked!",
                    "&4" + Message.BAR);
            return;
        }
        List<String> playerSwears = new ArrayList<>();
        for (String word : message.split(" ")) {
            String res = word.replaceAll("[^a-zA-Z]", "");
            for (String swear : Archon.getInstance().getSettings().getBlockedWords().keySet()) {
                if (res.equalsIgnoreCase(swear)) {
                    playerSwears.add(res);
                }
            }
        }
        if (!playerSwears.isEmpty()) {
            player.setSwearWarns(player.getSwearWarns() + 1);
            if (player.getSwearWarns() >= 5) {
                player.setSwearWarns(0);
                archon.notifyStaff(player.getDisplayName() + " &7has been temp-banned for swearing &c5/5 &7times.");
                long converted = 60000 * 10;
                archon.getPunishManager().tempban(player, System.currentTimeMillis() + converted, "&cYou have been temp-banned for swearing after being warned 5 times!", "m", null, converted);
                return;
            }
            String msg = "&7" + message;
            for (String s : playerSwears) {
                msg = msg.replace(s, "&4&m" + s + "&7");
            }
            player.message(
                    "&4" + Message.BAR,
                    "&c&lPLEASE STOP SWEARING &8&l> &7\"" + msg + "&7\"",
                    "&6You have &c&l" + (5 - player.getSwearWarns()) + " &6" + (((5 - player.getSwearWarns()) == 1) ? "warning" : "warnings") + " left before you get kicked!",
                    "&4" + Message.BAR);
        }
    }

    private static int uppercaseCount(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                count++;
            }
        }
        return count;
    }
}
