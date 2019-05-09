package de.minebench.minequery;

import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public interface MinequeryPlugin {
    Logger getLogger();

    String getPassword();

    boolean isIncluded(String name);

    void log(String message);

    int getListenerPort();

    QueryData getQueryData(Socket socket);

    QueryServer getQueryServer();

    List<String> executeCommand(String command);

    List<String> executeCommand(String name, String command);

    boolean executePlayerCommand(String player, String command);
}
