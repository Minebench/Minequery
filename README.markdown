# Minequery 1.7

Minequery is a server plugin for the Minecraft server proxy BungeeCord and the server mod Bukkit. It creates a small server listening for requests and responds with of the Minecraft server port, how many players are online, and the player list.

## Configuring

There are settings in you should setup in `plugins/Minequery/minequery.properties`

Example:

    server-ip=127.0.0.1

`server-ip` is the ip of the bungee proxy listener this query is for.

    server-port=25565

`server-port` is the port on which the bungee listener listens on and where users should connect.

    minequery-port=25566

`minequery-port` is the port on which the query server runs on.

    password=

`password` is the password that is necessary for sending commands to the server. Password protected functions are disabled with an empty password!

    logging=true

`logging` enables some more detailed logging of every request to the console.

### Bukkit specific settings

    included-worlds=*

`included-worlds` sets the worlds that should be included in the player list. Use commas to separate world names or `*` for all worlds.

    hidden-worlds=hidden1,hidden2

`hidden-worlds` sets the worlds that should be hidden in the player list. Use commas to separate world names.

### Bungee specific settings

    included-servers=*

`included-servers` sets the servers that should be included in the player list. Use commas to separate server names or `*` for all server.

    hidden-servers=hidden1,hidden2

`hidden-servers` sets the servers that should be hidden in the player list. Use commas to separate server names.

## License

Copyright (c) 2011 Vex Software LLC, released under the GPL v3.

Copyright (c) 2018 Minebench.de, released under the GPL v3.