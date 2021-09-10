package net.thearchon.hq;

public enum Rank {

    DEFAULT("", '7'),

    VIP("VIP", 'a'),
    VIP_PLUS("VIP&2&l+", 'a'),
    MVP("MVP", 'b'),
    MVP_PLUS("MVP&3&l+", 'b'),

    YOUTUBER("YouTuber", 'e'),

    HELPER("Helper", '3'),
    HELPER_PLUS("Helper+", '3'),
    TRIAL_MOD("Trial-Mod", 'd'),
    JR_MOD("Jr-Mod", '5'),
    MOD("Mod", '5'),
    SR_MOD("Sr-Mod", '5'),
    HEAD_MOD("Head-Mod", '6'),
    MANAGER("Manager", '2'),
    ADMIN("Admin", 'c'),
    OWNER("Owner", 'c');

    private final String name, color, prefix;

    Rank(String name, char color) {
        this.name = name;      
        this.color = "&" + color;
        prefix = this.color + "&l" + name.toUpperCase();
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplay() {
        return color + name;
    }

    public boolean hasPermission(Rank check) {
        return hasPermission(this, check);
    }

    public static boolean hasPermission(Rank rank, Rank check) {
        return rank.ordinal() >= check.ordinal();
    }

    public static boolean isPlayer(Rank rank) {
        return rank == DEFAULT || rank == VIP
                || rank == VIP_PLUS || rank == MVP
                || rank == MVP_PLUS || rank == YOUTUBER;
    }
    
    public static boolean isDonor(Rank rank) {
        return rank == VIP || rank == VIP_PLUS
                || rank == MVP || rank == MVP_PLUS;
    }
}
