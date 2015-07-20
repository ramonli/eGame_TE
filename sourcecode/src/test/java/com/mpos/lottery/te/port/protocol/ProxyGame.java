package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.gamespec.game.Game;

public class ProxyGame extends Game {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
