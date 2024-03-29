package de.minebench.minequery.bukkit;

import de.minebench.minequery.QuerySender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitQuerySender extends QuerySender implements ConsoleCommandSender {
    private final Minequery plugin;
    private Spigot spigot;

    public BukkitQuerySender(Minequery plugin, String name) {
        super(plugin, name);
        this.plugin = plugin;
        spigot = new QuerySpigot();
    }

    public BukkitQuerySender(Minequery plugin) {
        this(plugin, plugin.getName());
    }

    @Override
    public void sendMessage(String message) {
        response.add(message);
        plugin.getLogger().info(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String[] messages) {
        sendMessage(messages);
    }

    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public Spigot spigot() {
        return spigot;
    }

    @Override
    public Component name() {
        return LegacyComponentSerializer.legacySection().deserialize(getName());
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

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {

    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {

    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {

    }

    @Override
    public void sendRawMessage(String message) {
        sendMessage(message);
    }

    @Override
    public void sendRawMessage(UUID uuid, String message) {
        sendRawMessage(message);
    }

    private class QuerySpigot extends Spigot {
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
            BukkitQuerySender.this.sendMessage(TextComponent.toLegacyText(component));
        }

        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
            BukkitQuerySender.this.sendMessage(TextComponent.toLegacyText(components));
        }
    }
}
