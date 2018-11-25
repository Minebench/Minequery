package de.minebench.minequery;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Request extends Thread {
    private final MinequeryPlugin plugin;
    private final Socket socket;
    private String request = null;
    private boolean authenticated = false;
    private List<String> input = new ArrayList<>();

    public Request(MinequeryPlugin plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
    }

    public void run() {
        try {
            parseRequest();
            handleRequest();

            socket.close();
        } catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Exception while handling request", ex);
        }
    }

    private void parseRequest() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (request == null) {
                request = line;
            } else if (!authenticated) {
                authenticated = !plugin.getPassword().isEmpty() && line.equals(plugin.getPassword());
            } else {
                input.add(line);
            }
        }
    }

    private void handleRequest() throws IOException {
        if (request == null) {
            return;
        }

        plugin.log(socket.getInetAddress().getHostAddress() + " - '" + request + "' - " + authenticated + (input.isEmpty() ? "" : " - ['" + String.join("','" + "']")));

        if ("QUERY".equalsIgnoreCase(request) || "QUERY_JSON".equalsIgnoreCase(request)) {
            QueryData queryData = plugin.getQueryData(socket);

            if (queryData.disconnected != null) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeBytes(queryData.disconnected);
                return;
            }

            int onlinePlayers = queryData.getOnline();
            if (onlinePlayers > queryData.getNameList().size()) {
                onlinePlayers = queryData.getNameList().size();
            }

            Response response = new Response(
                    plugin.getListenerPort(),
                    queryData.getNameList(),
                    queryData.getUuidList(),
                    onlinePlayers,
                    queryData.getMaxPlayers()
            );

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            if (request.toUpperCase().endsWith("JSON")) {
                out.writeBytes(response.toJson());
            } else {
                out.writeBytes(response.toString());
            }
        } else if (authenticated) {
            List<String> response = new ArrayList<>();
            if ("COMMAND".equalsIgnoreCase(request)) {
                for (String command : input) {
                    response.add("COMMAND:" + command);
                    response.addAll(plugin.executeCommand(command));
                }
            } else if ("PLAYER_COMMAND".equalsIgnoreCase(request)) {
                for (String command : input) {
                    String[] parts = command.split(":", 2);
                    response.add("PLAYER_COMMAND:" + command + ":" + plugin.executeCommand(parts[0], parts[1]));
                }
            }
            if (!response.isEmpty()) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                for (String s : response) {
                    out.writeBytes(s);
                }
            }
        } else {
            plugin.getLogger().log(Level.WARNING, socket.getInetAddress().getHostAddress() + " tried to request '" + request + "' which is not supported?");
        }
    }

    public class Response {
        private final int serverPort;
        private final List<String> playerList;
        private final List<UUID> uuidList;
        private final int onlineCount;
        private final int maxPlayers;

        public Response(int serverPort, List<String> playerList, List<UUID> uuidList, int onlineCount, int maxPlayers) {
            this.serverPort = serverPort;
            this.playerList = playerList;
            this.uuidList = uuidList;
            this.onlineCount = onlineCount;
            this.maxPlayers = maxPlayers;
        }

        public String toString() {
            return "SERVERPORT " + serverPort + "\n" +
                    "PLAYERCOUNT " + onlineCount + "\n" +
                    "MAXPLAYERS " + maxPlayers + "\n" +
                    "PLAYERLIST " + Arrays.toString(playerList.toArray()) + "\n" +
                    "UUIDLIST " + Arrays.toString(uuidList.toArray()) + "\n";
        }

        public String toJson() {
            return "{" +
                    "\"serverPort\":" + serverPort + "," +
                    "\"playerCount\":" + onlineCount + "," +
                    "\"maxPlayers\":" + maxPlayers + "," +
                    "\"playerList\":[" + playerList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]," +
                    "\"uuidList\":[" + uuidList.stream().map(u -> "\"" + u.toString() + "\"").collect(Collectors.joining(",")) + "]" +
                    "}\n";
        }
    }
}
