package de.minebench.minequery.bungee;

import de.minebench.minequery.MinequeryPlugin;
import de.minebench.minequery.QueryData;
import de.minebench.minequery.QueryServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class Minequery extends Plugin implements MinequeryPlugin, Listener {
    public static final String CONFIG_FILE = "minequery.properties";
    private String serverIP;
    private int port;
    private QueryServer server;
    private String password;
    private boolean logging;
    private Set<String> includedServers;
    private Set<String> hiddenServers;
    private ScheduledTask serverTask;
    private ListenerInfo info;

    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        load();
    }

    private void load() {
        info = getProxy().getConfig().getListeners().iterator().next();
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
                serverIP = info.getHost().getAddress().getHostAddress();
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
            serverTask = getProxy().getScheduler().runAsync(this, server);
            //server = new QueryServer(this, serverIP, port);
            //server.start();
        } catch(IOException ex) {
            getLogger().log(Level.SEVERE, "Error initializing Minequery", ex);
        }
    }

    @EventHandler
    public void onReload(ProxyReloadEvent event) {
        onDisable();
        load();
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
        return info.getHost().getPort();
    }

    @Override
    public QueryData getQueryData(Socket socket) {
        QueryPendingConnection data = new QueryPendingConnection(this, socket);
        ProxyPingEvent pingEvent = new ProxyPingEvent(
                data,
                new ServerPing(
                        new ServerPing.Protocol(getDescription().getName(), 0),
                        new ServerPing.Players(info.getMaxPlayers(), getProxy().getOnlineCount(), new ServerPing.PlayerInfo[0]),
                        info.getMotd(),
                        ""
                ),
                (result, error) -> {}
        );
        getProxy().getPluginManager().callEvent(pingEvent);

        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getServer() != null && isIncluded(player.getServer().getInfo().getName())) {
                data.getNameList().add(player.getName());
                data.getUuidList().add(player.getUniqueId());
            }
        }

        data.setOnline(pingEvent.getResponse().getPlayers().getOnline());
        data.setMaxPlayers(pingEvent.getResponse().getPlayers().getMax());

        return data;
    }

    @Override
    public QueryServer getQueryServer() {
        return server;
    }

    @Override
    public List<String> executeCommand(String command) {
        BungeeQuerySender querySender = new BungeeQuerySender(this);
        getLogger().info("Executing: " + command);
        getProxy().getPluginManager().dispatchCommand(querySender, command);
        return querySender.getResponse();
    }

    @Override
    public boolean executeCommand(String player, String command) {
        ProxiedPlayer p = getProxy().getPlayer(player);
        if (p == null && player.length() == 36) {
            try {
                p = getProxy().getPlayer(UUID.fromString(player));
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.WARNING, player + " is not a valid UUID!");
            }
        }
        if (p != null) {
            getLogger().info("Making " + p.getName() + " execute:" + command);
            return getProxy().getPluginManager().dispatchCommand(p, command);
        }
        return false;
    }

    public ListenerInfo getListenerInfo() {
        return info;
    }
}
