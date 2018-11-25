package de.minebench.minequery.bungee;

import de.minebench.minequery.QuerySender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;
import java.util.HashSet;

public class BungeeQuerySender extends QuerySender implements CommandSender {

    private Minequery plugin;

    public BungeeQuerySender(Minequery plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public void sendMessage(String s) {
        response.add(s);
        plugin.getLogger().info(s);
    }

    @Override
    public void sendMessages(String... strings) {
        for (String string : strings) {
            sendMessage(string);
        }
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        sendMessage(TextComponent.toLegacyText(baseComponents));
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        sendMessage(TextComponent.toLegacyText(baseComponent));
    }

    @Override
    public Collection<String> getGroups() {
        return null;
    }

    @Override
    public void addGroups(String... strings) {

    }

    @Override
    public void removeGroups(String... strings) {

    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public void setPermission(String s, boolean b) {

    }

    @Override
    public Collection<String> getPermissions() {
        return new HashSet<>();
    }
}
