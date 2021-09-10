package net.thearchon.hq.service.buycraft;

import java.util.Arrays;
import java.util.regex.Pattern;

public class PackageCommand implements Comparable<Object> {
    
    private static final Pattern REPLACE_NAME = Pattern.compile(
            "[{\\(<\\[](name|player|username)[}\\)>\\]]", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPLACE_UUID = Pattern.compile(
            "[{\\(<\\[](uuid)[}\\)>\\]]");
    
    private final int id;
    private final String uuid;
    private final String username;
    private final String command;
    private final String label;
    private final String[] args;

    public PackageCommand(int id, String uuid, String username, String command) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;

        String parsed = REPLACE_NAME.matcher(command).replaceAll(username);
        if (uuid != null) {
            parsed = REPLACE_UUID.matcher(parsed).replaceAll(uuid);
        }
        this.command = parsed;

        String[] spl = parsed.split(" ");
        label = spl[0].toLowerCase();
        args = Arrays.copyOfRange(spl, 1, spl.length);
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getCommand() {
        return command;
    }

    public String getLabel() {
        return label;
    }

    public String[] getArgs() {
        return args;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Object o) {
        // If the objects are the same return 0
        if (this == o)
            return 0;

        if (o.getClass() == Integer.class) {
            return compareTo((Integer) o);
        } else if (o instanceof PackageCommand) {
            return compareTo((PackageCommand) o);
        }

        // Just do something random
        return hashCode() > o.hashCode() ? 1 : -1;
    }

    public int compareTo(PackageCommand o) {
        if (id == o.id) {
            return 0;
        }
        return id > o.id ? 1 : -1;
    }

    public int compareTo(Integer i) {
        return id > i ? 1 : id == i ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PackageCommand other = (PackageCommand) obj;
        return id == other.id;
    }
}
