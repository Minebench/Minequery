package de.minebench.minequery;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueryData {
    protected String disconnected = null;
    protected int online = 0;
    protected List<String> nameList = new ArrayList<>();
    protected List<UUID> uuidList = new ArrayList<>();
    protected int maxPlayers = 0;

    public int getOnline() {
        return online;
    }

    public List<String> getNameList() {
        return nameList;
    }

    public List<UUID> getUuidList() {
        return uuidList;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
