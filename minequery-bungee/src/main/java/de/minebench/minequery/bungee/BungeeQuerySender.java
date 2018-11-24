package de.minebench.minequery.bungee;

import de.minebench.minequery.MinequeryPlugin;
import de.minebench.minequery.QuerySender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import javax.swing.ImageIcon;
import java.util.Collection;
import java.util.Collections;
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
    }

    @Override
    public void sendMessages(String... strings) {
        Collections.addAll(response, strings);
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        response.add(TextComponent.toLegacyText(baseComponents));
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        response.add(TextComponent.toLegacyText(baseComponent));
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
