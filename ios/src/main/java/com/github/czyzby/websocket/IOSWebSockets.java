package com.github.czyzby.websocket;

import com.github.czyzby.websocket.WebSockets.WebSocketFactory;
import com.github.czyzby.websocket.impl.NvWebSocket;

import javax.net.ssl.SSLSocketFactory;

/** Allows to initiate common web sockets module.
 *
 * @author MJ */
public class IOSWebSockets {
    private IOSWebSockets() {
    }

    /** Initiates {@link WebSocketFactory}. */
    public static void initiate() {
        WebSockets.FACTORY = new IOSWebSocketFactory();
    }

    /** Provides {@link NvWebSocket} instances.
     *
     * @author MJ */
    protected static class IOSWebSocketFactory implements WebSocketFactory {
        @Override
        public WebSocket newWebSocket(final String url) {
            NvWebSocket nvWebSocket = new NvWebSocket(url);
            return nvWebSocket;
        }
    }
}
