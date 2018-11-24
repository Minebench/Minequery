package de.minebench.minequery.bungee;

import de.minebench.minequery.QueryData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.PendingConnection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public class QueryPendingConnection extends QueryData implements PendingConnection {
    private final Minequery plugin;
    private final Socket socket;

    public QueryPendingConnection(Minequery plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return (InetSocketAddress) plugin.getQueryServer().getListener().getLocalSocketAddress();
    }

    @Override
    public ListenerInfo getListener() {
        return plugin.getListenerInfo();
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public void setUniqueId(UUID uuid) {

    }

    @Override
    public boolean isOnlineMode() {
        return plugin.getProxy().getConfig().isOnlineMode();
    }

    @Override
    public void setOnlineMode(boolean onlineMode) {

    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
    }

    @Override
    public void disconnect(String reason) {
        this.disconnected = reason;
    }

    @Override
    public void disconnect(BaseComponent... reason) {
        disconnect(TextComponent.toPlainText(reason));
    }

    @Override
    public void disconnect(BaseComponent reason) {
        disconnect(TextComponent.toPlainText(reason));
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }
}
