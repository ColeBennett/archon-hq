package net.thearchon.hq.command;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.command.admin.*;
import net.thearchon.hq.command.all.*;
import net.thearchon.hq.command.all.info.*;
import net.thearchon.hq.command.helper.*;
import net.thearchon.hq.command.helperplus.MuteCommand;
import net.thearchon.hq.command.helperplus.TempbanCommand;
import net.thearchon.hq.command.jrmod.AccountsCommand;
import net.thearchon.hq.command.manager.*;
import net.thearchon.hq.command.mod.BanCommand;
import net.thearchon.hq.command.mod.UnbanCommand;
import net.thearchon.hq.command.mod.UnmuteCommand;
import net.thearchon.hq.command.trialmod.WhereCommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class CommandManager implements ICommandManager {

    private final Archon archon;
    private final Map<String, Command> commands = new HashMap<>();

    public CommandManager(Archon archon) {
        this.archon = archon;

        register("infectious", new Command() {
            @Override
            public void execute(Player player, String[] args) {
                player.message("&6>> &3https://www.thearchon.net/community/threads/new-season-misc-information.5482/#post-26909");
            }
        }, Rank.DEFAULT);
        register("silver", new Command() {
            @Override
            public void execute(Player player, String[] args) {
                player.message("&3&k# &6https://www.thearchon.net/community/threads/kronos-dimension-reset-f-top.5530/ &3&k#");
            }
        }, Rank.DEFAULT, "kronos");

        register("plugins", new Command() {
            @Override
            public void execute(Player player, String[] args) {
                player.message("&fPlugins (1): &aArchonSuite");
            }
        }, Rank.DEFAULT, "?", "pl");

        register("twitter", new TwitterCommand(), Rank.DEFAULT);
        register("teamspeak", new TeamSpeakCommand(), Rank.DEFAULT, "ts");
        register("discord", new DiscordCommand(), Rank.DEFAULT);
        register("website", new WebsiteCommand(), Rank.DEFAULT, "site", "forum", "forums");
//        registerAll(new ShopCommand(), Rank.DEFAULT, "donate", "store", "shop");
        register("rules", new RulesCommand(), Rank.DEFAULT);

        register("votetop", new VoteTopCommand(archon), Rank.DEFAULT);
        register("transfer", new TransferCommand(archon), Rank.DEFAULT);
        register("lobby", new ToLobbyCommand(archon), Rank.DEFAULT, "hub");
        register("vote", new VoteInfoCommand(archon), Rank.DEFAULT);
        register("claim", new VoteClaimCommand(archon), Rank.DEFAULT);
        register("beta", new BetaCommand(archon), Rank.DEFAULT);
//        registerAll(new PartyCommand(archon), Rank.DEFAULT, "p2");
        register("server", new ServerCommand(archon), Rank.DEFAULT);
        register("glist", new GListCommand(archon), Rank.DEFAULT);
        register("list", new ListCommand(archon), Rank.DEFAULT, "playerlist", "who", "playertable", "online");
        register("lobbylist", new LobbyListCommand(archon), Rank.DEFAULT, "llist");

        /*
         * Helper Commands
         */
        register("warn", new WarnCommand(archon), Rank.HELPER);
        register("kick", new KickCommand(archon), Rank.HELPER, "ekick");
        register("stafflist", new StafflistCommand(archon), Rank.HELPER);
        register("sc", new StaffChatCommand(archon), Rank.HELPER);
        register("staffsilent", new StaffChatCommand(archon), Rank.HELPER);
        register("staffchat", new StaffChatCommand(archon), Rank.HELPER);
        register("seen", new SeenCommand(archon), Rank.HELPER);
        register("info", new InfoCommand(archon), Rank.HELPER);

        /*
         * Helper+ Commands
         */
        register("mute", new MuteCommand(archon), Rank.HELPER_PLUS, "emute");
        register("unmute", new UnmuteCommand(archon), Rank.HELPER_PLUS, "eunmute");
        register("tempban", new TempbanCommand(archon), Rank.HELPER_PLUS, "etempban");

        /*
         * Trial-Mod Commands
         */
        register("where", new WhereCommand(archon), Rank.TRIAL_MOD);

        /*
         * Jr-Mod Commands
         */
        register("accounts", new AccountsCommand(archon), Rank.JR_MOD);
        register("ban", new BanCommand(archon), Rank.JR_MOD, "eban");
        register("unban", new UnbanCommand(archon), Rank.JR_MOD, "eunban");

        /*
         * Mod Commands
         */

        /*
         * Sr-Mod Commands
         */
        register("banip", new BanIpCommand(archon), Rank.SR_MOD);

        /*
         * Head-Mod Commands
         */

        /*
         * Manager Commands
         */
        register("transaction", new TransactionCommand(archon), Rank.MANAGER);
        register("gmute", new GlobalMuteCommand(archon), Rank.MANAGER);
        register("unbanip", new UnbanIpCommand(archon), Rank.MANAGER);
        register("rank", new RankCommand(archon), Rank.MANAGER);
        register("alert", new AlertCommand(archon), Rank.MANAGER);
        register("send", new SendCommand(archon), Rank.MANAGER);
        register("maintenance", new MaintenanceCommand(archon), Rank.MANAGER);
        register("hosts", new HostsCommand(archon), Rank.MANAGER);
        register("proxies", new ProxiesCommand(archon), Rank.MANAGER);
        register("regions", new RegionsCommand(archon), Rank.MANAGER);
        register("staffinfo", new StaffInfoCommand(archon), Rank.MANAGER);
        register("staffhistory", new StaffHistoryCommand(archon), Rank.MANAGER);
        register("kickall", new KickAllCommand(archon), Rank.MANAGER);

        /*
         * Admin Commands
         */
        register("setslots", new SetSlotsCommand(archon), Rank.ADMIN);
        register("reload", new ReloadCommand(archon), Rank.ADMIN);
        register("runall", new RunAllCommand(archon), Rank.ADMIN);
        register("register", new RegisterCommand(archon), Rank.ADMIN);
        register("appinfo", new AppInfoCommand(), Rank.ADMIN);
        register("givecoins", new GiveCoinsCommand(archon), Rank.ADMIN);
        register("ip", new IpCommand(archon), Rank.ADMIN);
        register("ipinfo", new IpInfoCommand(archon), Rank.ADMIN);
        register("ubungeeconfig", new UBungeeConfigCommand(archon), Rank.ADMIN);
        register("runcmd", new RunCommand(archon), Rank.ADMIN);
        register("restart", new RestartCommand(archon), Rank.ADMIN);
        register("echo", new EchoCommand(), Rank.ADMIN);
        register("shutdown", new ShutdownCommand(archon), Rank.ADMIN);
        register("update", new UpdateCommand(archon), Rank.ADMIN);
        register("updatejar", new UpdateJarCommand(archon), Rank.ADMIN);

        /*
         * Owner Commands
         */
//        register("unbanall", new UnbanAllCommand(archon), Rank.OWNER);
    }

    @Override
    public void register(String name, Command command, Rank permission) {
        command.setPermission(permission);
        commands.put(name, command);
    }

    @Override
    public void register(String name, Command command, Rank permission, String... aliases) {
        command.setPermission(permission);
        commands.put(name, command);
        for (String alias : aliases) {
            commands.put(alias, command);
        }
    }

    @Override
    public void unregister(String name) {
        commands.remove(name);
    }

    @Override
    public void unregisterAll() {
        commands.clear();
    }

    @Override
    public boolean isCommand(String name) {
        return commands.containsKey(name);
    }

    @Override
    public Command getCommand(String name) {
        return commands.get(name);
    }

    @Override
    public Set<String> getCommandNames() {
        return commands.keySet();
    }

    @Override
    public ConsoleSender getConsoleSender() {
        return ConsoleSender.INSTANCE;
    }

    @Override
    public void setPermission(String name, Rank permission) {
        Command command = commands.get(name);
        if (command == null) {
            throw new NullPointerException(name);
        }
        command.setPermission(permission);
    }

    @Override
    public void runCommand(String cmdName, Player player, String[] args) {
        Command command = commands.get(cmdName);
        if (command == null) {
            archon.getLogger().info("Unknown command: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]");
            return;
        }
        if (command.getPermission() == null) {
            player.error("Command '" + cmdName + "' does not have a permission set.");
            return;
        }
        if (player.hasPermission(command.getPermission())) {
            try {
                archon.getLogger().info("Executing: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "] ...");
                long start = System.currentTimeMillis();
                command.execute(player, args);
                long duration = System.currentTimeMillis() - start;
                archon.getLogger().info("Executed: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "] - Took " + duration + " ms");
                if (duration >= 1000) {
                    archon.getLogger().warning("Command execution took too long (" + duration + " ms): " + cmdName);
                }
            } catch (Exception e) {
                player.error("An error occured while executing command: " + cmdName);
                archon.getLogger().log(Level.SEVERE, "Failed to execute: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]", e);
            }
        } else {
            player.message(Message.UNKNOWN_COMMAND);
            archon.getLogger().warning("Permission denied: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]");
        }
    }
}
