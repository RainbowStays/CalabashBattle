package nju.zc.calabashbattle.client.model;

public class Hall{
    private final String username;
    private final String serverIP;
    private final int port;

    public Hall(String name, String serverIP, int port) {
        this.username = name;
        this.serverIP = serverIP;
        this.port = port;
    }

    public String getUsername(){
        return username;
    }

    public String getServerIP(){
        return serverIP;
    }

    public int getPort(){
        return port;
    }
}