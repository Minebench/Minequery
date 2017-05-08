package net.minestatus.minequery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;

import net.md_5.bungee.api.plugin.Plugin;

public final class Minequery extends Plugin {
    public static final String CONFIG_FILE = "minequery.properties";
    private String serverIP;
    private int port;
    private QueryServer server;
    private String password;
    private boolean logging;

    public void onEnable() {
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

                OutputStream out = new FileOutputStream(file);
                props.store(out, "Minequery settings");
            }
            Properties props = new Properties();
            props.load(new FileReader(file));

            serverIP = props.getProperty("server-ip", "ANY");
            port = Integer.parseInt(props.getProperty("minequery-port", "25566"));
            password = props.getProperty("password", "");
            logging = Boolean.parseBoolean(props.getProperty("logging", "true"));
            if(serverIP.equals("")) {
                serverIP = "ANY";
            }
            server = new QueryServer(this, serverIP, port);
            getProxy().getScheduler().runAsync(this, server);
            //server = new QueryServer(this, serverIP, port);
            //server.start();
        } catch(IOException ex) {
            getLogger().log(Level.SEVERE, "Error initializing Minequery", ex);
        }
    }

    public void onDisable() {
        try {
            server.getListener().close();
        } catch(IOException ex) {
            getLogger().log(Level.WARNING, "Unable to close the Minequery listener", ex);
        } catch(NullPointerException e) {
            getLogger().log(Level.WARNING, "Server was never initialized!");
        }
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public void log(String message) {
        if (logging) {
            getLogger().log(Level.INFO, message);
        }
    }
}
