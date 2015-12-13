package net.minestatus.minequery;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class Request extends Thread {
    private final Minequery plugin;
    private final Socket socket;

    public Request(Minequery plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
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
        if(request.equalsIgnoreCase("QUERY")) {
            List<String> playerList = new ArrayList<String>();
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                playerList.add(player.getName());
            }


            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(
                    "SERVERPORT " + plugin.getServerPort() + "\n" +
                    "PLAYERCOUNT " + ProxyServer.getInstance().getPlayers().size() + "\n" +
                    "MAXPLAYERS " + plugin.getMaxPlayers() + "\n" +
                    "PLAYERLIST " + Arrays.toString(playerList.toArray()) + "\n"
            );
        }
        if(request.equalsIgnoreCase("QUERY_JSON")) {

            StringBuilder resp = new StringBuilder();
            resp.append("{");
            resp.append("\"serverPort\":").append(plugin.getServerPort()).append(",");
            resp.append("\"playerCount\":").append(ProxyServer.getInstance().getPlayers().size()).append(",");
            resp.append("\"maxPlayers\":").append(plugin.getMaxPlayers()).append(",");
            resp.append("\"playerList\":");
            resp.append("[");


            String prefix = "";
            for(ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                resp.append(prefix);
                prefix = ",";
                resp.append("\"").append(player.getName()).append("\"");
            }
            resp.append("]");
            resp.append("}\n");


            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(resp.toString());
        }
    }
}
