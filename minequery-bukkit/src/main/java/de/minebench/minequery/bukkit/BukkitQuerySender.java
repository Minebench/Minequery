package de.minebench.minequery.bukkit;

import de.minebench.minequery.QuerySender;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BukkitQuerySender extends QuerySender implements CommandSender {
    private final Minequery plugin;
    private Spigot spigot;

    public BukkitQuerySender(Minequery plugin) {
        super(plugin);
        this.plugin = plugin;
        spigot = new QuerySpigot();
    }

    @Override
    public void sendMessage(String message) {
        response.add(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        Collections.addAll(response, messages);
    }

    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public Spigot spigot() {
        return spigot;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(String name) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {

    }

    private class QuerySpigot extends Spigot {
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
            response.add(TextComponent.toLegacyText(component));
        }

        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
            response.add(TextComponent.toLegacyText(components));
        }
    }
}
