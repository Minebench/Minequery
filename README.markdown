# Minequery 1.5

Minequery is a server plugin for the Minecraft server proxy BungeeCord. It creates a small server listening for requests and responds with of the Minecraft server port, how many players are online, and the player list.

Looking for the Bukkit version of Minequery? It has been moved to the [hmod](https://github.com/vexsoftware/minequery/) branch.

Looking for the old hMod version of Minequery? It has been moved to the [hmod](https://github.com/vexsoftware/minequery/tree/hmod) branch.

## Configuring

There are settings in you should setup in `plugins/Minequery/minequery.properties`

Example:

    server-ip=127.0.0.1

`server-ip` is the ip of the bungee proxy listener this query is for.

    server-port=25565

`server-port` is the port on which the bungee listener listens on and where users should connect.

    minequery-port=25566

`minequery-port` is the port on which the query server runs on.

    minequery-port=25566

`max-players` is the maximum amount of players this server can hold.

## License

Copyright (c) 2011 Vex Software LLC, released under the GPL v3.