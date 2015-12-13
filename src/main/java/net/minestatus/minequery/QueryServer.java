package net.minestatus.minequery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class QueryServer implements Runnable {
    private final Minequery plugin;
    private final ServerSocket listener;

    public QueryServer(Minequery plugin, String host, int port) throws IOException {
        this.plugin = plugin;
        InetSocketAddress address;
        if(host.equalsIgnoreCase("ANY")) {
            plugin.getLogger().info("Starting server on *:" + Integer.toString(port));
            address = new InetSocketAddress(port);
        } else {
            plugin.getLogger().info("Starting server on " + host + ":" + Integer.toString(port));
            address = new InetSocketAddress(host, port);
        }
        listener = new ServerSocket();
        listener.bind(address);
    }

    public void run() {
        try {
            for(; ; ) {
                Socket socket = listener.accept();


                new Thread(new Request(plugin, socket)).start();
            }
        } catch(IOException ex) {
            plugin.getLogger().info("Stopping server");
        }
    }

    public ServerSocket getListener() {
        return listener;
    }
}
