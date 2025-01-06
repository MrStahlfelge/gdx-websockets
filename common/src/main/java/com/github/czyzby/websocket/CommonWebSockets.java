package com.github.czyzby.websocket;

import com.github.czyzby.websocket.WebSockets.WebSocketFactory;
import com.github.czyzby.websocket.impl.NvWebSocket;

import java.util.List;

/** Allows to initiate common web sockets module.
 *
 * @author MJ */
public class CommonWebSockets {
    private CommonWebSockets() {
    }

    /** Initiates {@link WebSocketFactory}. */
    public static void initiate() {
        WebSockets.FACTORY = new CommonWebSocketFactory();
    }

    /** Provides {@link NvWebSocket} instances.
     *
     * @author MJ */
    protected static class CommonWebSocketFactory implements WebSocketFactory {
        @Override
        public WebSocket newWebSocket(final String url, final List<String> protocols) {
            return new NvWebSocket(url, protocols);
        }
    }
}
