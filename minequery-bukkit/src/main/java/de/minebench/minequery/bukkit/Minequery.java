package de.minebench.minequery.bukkit;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.network.StatusClient;
import de.minebench.minequery.MinequeryPlugin;
import de.minebench.minequery.QueryData;
import de.minebench.minequery.QueryServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class Minequery extends JavaPlugin implements MinequeryPlugin {
    public static final String CONFIG_FILE = "minequery.properties";
    private String serverIP;
    private int port;
    private QueryServer server;
    private String password;
    private boolean logging;
    private Set<String> includedServers;
    private Set<String> hiddenServers;
    private BukkitTask serverTask;

    public void onEnable() {
        load();
    }

    private void load() {
        try {
            File dir = getDataFolder();
            File file = new File(dir, CONFIG_FILE);
            if((!dir.exists()) || (!file.exists())) {
                dir.mkdirs();
                getLogger().log(Level.INFO, "Generate config");

                Properties props = new Properties();
                props.setProperty("server-ip", "127.0.0.1");
                props.setProperty("minequery-port", "25566");
                props.setProperty("password", "");
                props.setProperty("logging", "true");
                props.setProperty("included-servers", "*");
                props.setProperty("hidden-servers", "hidden1,hidden2");

                OutputStream out = new FileOutputStream(file);
                props.store(out, "Minequery settings");
            }
            Properties props = new Properties();
            props.load(new FileReader(file));

            serverIP = props.getProperty("server-ip", "SELF");
            if (serverIP.isEmpty() || serverIP.equalsIgnoreCase("SELF")) {
                serverIP = getServer().getIp();
            }
            port = Integer.parseInt(props.getProperty("minequery-port", "25566"));
            password = props.getProperty("password", "");
            logging = Boolean.parseBoolean(props.getProperty("logging", "true"));
            includedServers = new HashSet<>(Arrays.asList(props.getProperty("included-servers", "*").toLowerCase().split(",")));
            hiddenServers = new HashSet<>(Arrays.asList(props.getProperty("hidden-servers", "").toLowerCase().split(",")));
            if(serverIP.equals("")) {
                serverIP = "ANY";
            }
            server = new QueryServer(this, serverIP, port);
            serverTask = getServer().getScheduler().runTaskAsynchronously(this, server);
            //server = new QueryServer(this, serverIP, port);
            //server.start();
        } catch(IOException ex) {
            getLogger().log(Level.SEVERE, "Error initializing Minequery", ex);
        }
    }

    public void onDisable() {
        try {
            if (server != null) {
                server.getListener().close();
            }
            if (serverTask != null) {
                serverTask.cancel();
            }
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Unable to close the Minequery listener", ex);
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isIncluded(String name) {
        boolean isIncluded = includedServers.isEmpty() || includedServers.contains("*") || includedServers.contains(name.toLowerCase());
        return isIncluded && !hiddenServers.contains(name.toLowerCase());
    }

    @Override
    public void log(String message) {
        if (logging) {
            getLogger().log(Level.INFO, message);
        }
    }

    @Override
    public int getListenerPort() {
        return getServer().getPort();
    }

    @Override
    public QueryData getQueryData(Socket socket) {
        QueryData data = new QueryData();

        PaperServerListPingEvent pingEvent = new PaperServerListPingEvent(
                new StatusClient() {
                    @Override
                    public InetSocketAddress getAddress() {
                        return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
                    }

                    @Override
                    public int getProtocolVersion() {
                        return 0;
                    }

                    @Override
                    public InetSocketAddress getVirtualHost() {
                        return null;
                    }
                },
                getServer().getMotd(),
                getServer().getOnlinePlayers().size(),
                getServer().getMaxPlayers(),
                getName(),
                0,
                null
        );
        getServer().getPluginManager().callEvent(pingEvent);

        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getServer() != null && isIncluded(player.getWorld().getName())) {
                data.getNameList().add(player.getName());
                data.getUuidList().add(player.getUniqueId());
            }
        }

        data.setOnline(pingEvent.getNumPlayers());
        data.setMaxPlayers(pingEvent.getMaxPlayers());

        return data;
    }

    @Override
    public QueryServer getQueryServer() {
        return server;
    }

    @Override
    public List<String> executeCommand(String command) {
        BukkitQuerySender querySender = new BukkitQuerySender(this);
        getLogger().info("Executing: " + command);
        getServer().dispatchCommand(querySender, command);
        return querySender.getResponse();
    }

    @Override
    public boolean executeCommand(String player, String command) {
        Player p = getServer().getPlayer(player);
        if (p == null && player.length() == 36) {
            try {
                p = getServer().getPlayer(UUID.fromString(player));
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.WARNING, player + " is not a valid UUID!");
            }
        }
        if (p != null) {
            getLogger().info("Making " + p.getName() + " execute:" + command);
            return getServer().dispatchCommand(p, command);
        }
        return false;
    }
}
