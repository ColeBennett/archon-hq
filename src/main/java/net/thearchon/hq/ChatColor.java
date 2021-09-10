package net.thearchon.hq;

import java.util.HashMap;
import java.util.Map;

public enum ChatColor {

    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    BOLD('l'),
    ITALIC('o'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    RESET('r');

    private final char code;
    private final String fullCode;

    ChatColor(char code) {
        this.code = code;
        fullCode = "&" + code;
    }

    public char getCode() {
        return code;
    }

    @Override
    public String toString() {
        return fullCode;
    }

    public static final ChatColor[] VALUES = values();
    private static final Map<Character, ChatColor> BY_CODE;

    static {
        BY_CODE = new HashMap<>(VALUES.length);
        for (ChatColor color : VALUES) {
            BY_CODE.put(color.code, color);
        }
    }

    public static ChatColor valueOf(char code) {
        return BY_CODE.get(code);
    }
}
