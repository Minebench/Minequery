package net.minestatus.minequery;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;

public final class Request extends Thread implements PendingConnection {
    private final Minequery plugin;
    private final Socket socket;
    private final ListenerInfo info;
    private String disconnected = null;

    public Request(Minequery plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
        info = plugin.getProxy().getConfig().getListeners().iterator().next();
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            handleRequest(socket, reader.readLine());

            socket.close();
        } catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Minequery server thread shutting down", ex);
        }
    }

    private void handleRequest(Socket socket, String request) throws IOException {
        if(request == null) {
            return;
        }

        plugin.log(getAddress() + " - '" + request + "'");

        ProxyPingEvent pingEvent = new ProxyPingEvent(
                this,
                new ServerPing(
                        new ServerPing.Protocol(plugin.getDataFolder().getName(), 0),
                        new ServerPing.Players(info.getMaxPlayers(), plugin.getProxy().getOnlineCount(), new ServerPing.PlayerInfo[0]),
                        info.getMotd(),
                        ""
                ),
                (result, error) -> {}
        );
        plugin.getProxy().getPluginManager().callEvent(pingEvent);
        if (disconnected != null) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(disconnected);
            return;
        }

        Response response = new Response(
                info.getHost().getPort(),
                plugin.getProxy().getPlayers(),
                pingEvent.getResponse().getPlayers().getOnline(),
                pingEvent.getResponse().getPlayers().getMax()
        );

        if (request.equalsIgnoreCase("QUERY" + plugin.getPassword())) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(response.toString());
        } else if(request.equalsIgnoreCase("QUERY_JSON" + plugin.getPassword())) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(response.toJson());
        } else {
            plugin.getLogger().log(Level.WARNING, getAddress() + " tried to request '" + request + "' which is not supported?");
        }
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return (InetSocketAddress) socket.getLocalSocketAddress();
    }

    @Override
    public ListenerInfo getListener() {
        return info;
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

    public class Response {
        private final int serverPort;
        private final int onlineCount;
        private final int maxPlayers;
        private final ArrayList<String> playerList;

        public Response(int serverPort, Collection<ProxiedPlayer> players, int onlineCount, int maxPlayers) {
            this.serverPort = serverPort;
            this.onlineCount = onlineCount;
            this.maxPlayers = maxPlayers;
            playerList = new ArrayList<>();
            for(ProxiedPlayer player : players) {
                playerList.add(player.getName());
            }
        }

        public String toString() {
            return "SERVERPORT " + serverPort + "\n" +
                    "PLAYERCOUNT " + onlineCount + "\n" +
                    "MAXPLAYERS " + maxPlayers + "\n" +
                    "PLAYERLIST " + Arrays.toString(playerList.toArray()) + "\n";
        }

        public String toJson() {
            StringBuilder resp = new StringBuilder();
            resp.append("{");
            resp.append("\"serverPort\":").append(serverPort).append(",");
            resp.append("\"playerCount\":").append(onlineCount).append(",");
            resp.append("\"maxPlayers\":").append(maxPlayers).append(",");
            resp.append("\"playerList\":");
            resp.append("[");
            if (!playerList.isEmpty()) {
                resp.append(playerList.get(0));
                for (int i = 1; i < playerList.size(); i++) {
                    resp.append(",").append("\"").append(playerList.get(i)).append("\"");
                }
            }
            resp.append("]");
            resp.append("}\n");
            return resp.toString();
        }
    }
}
