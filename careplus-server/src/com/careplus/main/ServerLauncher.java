package com.careplus.main;

import com.careplus.network.Server;

public class ServerLauncher {

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}