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
    private Type type = null;
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
        while ((line = reader.readLine()) != null && !line.equalsIgnoreCase("{END}")
                && (type == null || type.inputLength < 0 || input.size() < type.inputLength)) {
            if (type == null) {
                try {
                    type = Type.valueOf(line.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, socket.getInetAddress().getHostAddress() + " tried to request '" + type + "' which is not supported?");
                    break;
                }
            } else if (!authenticated) {
                authenticated = !plugin.getPassword().isEmpty() && line.equals(plugin.getPassword());
                if (!authenticated) {
                    break;
                }
            } else {
                input.add(line);
            }
        }
    }

    private void handleRequest() throws IOException {
        if (type == null) {
            return;
        }

        plugin.log(socket.getInetAddress().getHostAddress() + " - '" + type + "' - " + authenticated + (input.isEmpty() ? "" : " - ['" + String.join("','", input) + "']"));

        if (type.requiresAuthentication && !authenticated) {
            return;
        }

        List<String> responseList = new ArrayList<>();
        switch (type) {
            case QUERY:
            case QUERY_JSON:
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

                if (type == Type.QUERY_JSON) {
                    responseList.add(response.toJson());
                } else {
                    responseList.add(response.toString());
                }
                break;
            case COMMAND:
                for (String command : input) {
                    responseList.add("COMMAND:" + command);
                    responseList.addAll(plugin.executeCommand(command));
                }
                break;
            case PLAYER_COMMAND:
                for (String command : input) {
                    String[] parts = command.split(":", 2);
                    responseList.add("PLAYER_COMMAND:" + command + ":" + plugin.executeCommand(parts[0], parts[1]));
                }
                break;
            default:
                plugin.getLogger().log(Level.WARNING, socket.getInetAddress().getHostAddress() + " tried to request '" + type + "' which is not supported?");
        }
        if (!responseList.isEmpty()) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            for (String s : responseList) {
                out.writeBytes(s);
            }
        }
    }

    public enum Type {
        QUERY,
        QUERY_JSON,
        COMMAND(true, -1),
        PLAYER_COMMAND(true, -1);

        private final boolean requiresAuthentication;
        private final int inputLength;

        Type() {
            this(false, 0);
        }

        Type(boolean requiresAuthentication, int inputLength) {
            this.requiresAuthentication = requiresAuthentication;
            this.inputLength = inputLength;
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
