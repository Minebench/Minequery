package de.minebench.minequery.bukkit;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.network.StatusClient;
import com.google.common.collect.ImmutableMap;
import de.minebench.minequery.MinequeryPlugin;
import de.minebench.minequery.QueryData;
import de.minebench.minequery.QueryServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class Minequery extends JavaPlugin implements MinequeryPlugin, Listener {
    public static final String CONFIG_FILE = "minequery.properties";
    private static final Supplier<JSONObject> PERFORMANCE_DATA_BUILDER = () -> {
        JSONObject json = new JSONObject();
        JSONArray tpsArray = new JSONArray();
        for (double tps : Bukkit.getTPS()) {
            tpsArray.add(tps);
        }
        json.put("tps", tpsArray);
        json.put("mspt", Bukkit.getAverageTickTime());
        json.put("chunks", globalCount(World::getChunkCount));
        json.put("entities", globalCount(World::getEntityCount));
        json.put("tile-entities", globalCount(World::getTileEntityCount));
        json.put("tickable-tile-entities", globalCount(World::getTickableTileEntityCount));
        return json;
    };
    private String serverIP;
    private int port;
    private List<QueryServer> servers = new ArrayList<>();
    private String password;
    private boolean logging;
    private Set<String> includedWorlds;
    private Set<String> hiddenWorlds;

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
                props.setProperty("included-worlds", "*");
                props.setProperty("hidden-worlds", "hidden1,hidden2");

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
            includedWorlds = new HashSet<>(Arrays.asList(props.getProperty("included-worlds", "*").toLowerCase().split(",")));
            hiddenWorlds = new HashSet<>(Arrays.asList(props.getProperty("hidden-worlds", "").toLowerCase().split(",")));
            if(serverIP.equals("")) {
                serverIP = "ANY";
            }
            if (password.isEmpty()) {
                getLogger().log(Level.WARNING, "No password is set! Requests that require authentication will not work!");
            }
            getServer().getPluginManager().registerEvents(this, this);
        } catch(IOException ex) {
            getLogger().log(Level.SEVERE, "Error initializing Minequery", ex);
        }
    }

    @EventHandler
    public void onServerLoaded(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.RELOAD) {
            for (QueryServer server : servers) {
                try {
                    server.getListener().close();
                    server.interrupt();
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING, "Unable to close the Minequery listener", ex);
                }
            }
        }
        try {
            for (String s : serverIP.split(",")) {
                QueryServer server = new QueryServer(this, s, port);
                server.start();
                servers.add(server);
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error initializing Minequery Server", ex);
        }
    }

    public void onDisable() {
        for (QueryServer server : servers) {
            try {
                server.getListener().close();
                server.interrupt();
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Unable to close the Minequery listener", ex);
            }
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isIncluded(String name) {
        boolean isIncluded = includedWorlds.isEmpty() || includedWorlds.contains("*") || includedWorlds.contains(name.toLowerCase());
        return isIncluded && !hiddenWorlds.contains(name.toLowerCase());
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
                getServer().motd(),
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
    public String getPerformanceData() {
        if (getServer().isPrimaryThread()) {
            return PERFORMANCE_DATA_BUILDER.get().toJSONString();
        }
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        getServer().getScheduler().runTask(this,
                () -> future.complete(PERFORMANCE_DATA_BUILDER.get()));
        try {
            return future.get(1, TimeUnit.SECONDS).toJSONString();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return new JSONObject(ImmutableMap.of("error", e.getMessage())).toJSONString();
        }
    }

    private static int globalCount(Function<World, Integer> getter) {
        int amount = 0;
        for (World world : Bukkit.getWorlds()) {
            amount += getter.apply(world);
        }
        return amount;
    }

    @Override
    public QueryServer getQueryServer() {
        return servers.isEmpty() ? null : servers.get(0);
    }

    @Override
    public List<String> executeCommand(String command) {
        return executeCommand(new BukkitQuerySender(this), command);
    }

    @Override
    public List<String> executeCommand(String name, String command) {
        return executeCommand(new BukkitQuerySender(this, name), command);
    }

    private List<String> executeCommand(BukkitQuerySender querySender, String command) {
        getLogger().info("Executing: " + command);
        Object pauseLock = new Object();
        getServer().getScheduler().runTask(this, () -> {
            synchronized (pauseLock) {
                getServer().dispatchCommand(querySender, command);
                pauseLock.notify();
            }
        });

        synchronized (pauseLock) {
            try {
                pauseLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return querySender.getResponse();
    }

    @Override
    public boolean executePlayerCommand(String player, String command) {
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
            AtomicReference<Boolean> r = new AtomicReference<>(null);
            Player finalP = p;
            Object pauseLock = new Object();
            getServer().getScheduler().runTask(this, () -> {
                synchronized (pauseLock) {
                    r.set(getServer().dispatchCommand(finalP, command));
                    pauseLock.notify();
                }
            });
            synchronized (pauseLock) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return r.get() != null && r.get();
        }
        return false;
    }
}
