package com.nix.nixtp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NixTP extends JavaPlugin {

    private record Request(UUID from, long expiresAt) {}

    // key = the player who received the request
    private final Map<UUID, Request> pending = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        for (String name : new String[]{"tpa", "tpaccept", "tpdeny"}) {
            getCommand(name).setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "tpa" -> sendRequest(player, args);
            case "tpaccept" -> accept(player);
            case "tpdeny" -> deny(player);
            default -> { return false; }
        }
        return true;
    }

    private void sendRequest(Player player, String[] args) {
        if (args.length != 1) {
            msg(player, "Usage: /tpa <player>", NamedTextColor.RED);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            msg(player, "Player not found.", NamedTextColor.RED);
            return;
        }
        if (target.equals(player)) {
            msg(player, "You can't teleport to yourself.", NamedTextColor.RED);
            return;
        }

        long timeoutMs = getConfig().getLong("request-timeout-seconds", 60) * 1000L;
        pending.put(target.getUniqueId(), new Request(player.getUniqueId(), System.currentTimeMillis() + timeoutMs));

        msg(player, "Request sent to " + target.getName() + ".", NamedTextColor.GREEN);
        msg(target, player.getName() + " wants to teleport to you. Use /tpaccept or /tpdeny.", NamedTextColor.YELLOW);
    }

    private void accept(Player player) {
        Request request = pending.remove(player.getUniqueId());
        if (request == null || System.currentTimeMillis() > request.expiresAt()) {
            msg(player, "No pending request.", NamedTextColor.RED);
            return;
        }

        Player from = Bukkit.getPlayer(request.from());
        if (from == null) {
            msg(player, "That player is no longer online.", NamedTextColor.RED);
            return;
        }

        from.teleport(player.getLocation());
        msg(from, "Teleported to " + player.getName() + ".", NamedTextColor.GREEN);
        msg(player, "Request accepted.", NamedTextColor.GREEN);
    }

    private void deny(Player player) {
        Request request = pending.remove(player.getUniqueId());
        if (request == null || System.currentTimeMillis() > request.expiresAt()) {
            msg(player, "No pending request.", NamedTextColor.RED);
            return;
        }

        Player from = Bukkit.getPlayer(request.from());
        if (from != null) {
            msg(from, player.getName() + " denied your request.", NamedTextColor.RED);
        }
        msg(player, "Request denied.", NamedTextColor.YELLOW);
    }

    private void msg(Player player, String text, NamedTextColor color) {
        player.sendMessage(Component.text(text, color));
    }
}
