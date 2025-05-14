package com.github.czyzby.websocket;

import com.github.czyzby.websocket.impl.TeaWebSocket;

/** Allows to initiate TeaVM web sockets module.
 *
 * @author SimonIT */
public class TeaWebSockets {

	private TeaWebSockets() {
	}

	/** Initiates {@link WebSockets.WebSocketFactory}. */
	public static void initiate() {
		WebSockets.FACTORY = new TeaWebSocketFactory();
	}

	/** Provides {@link TeaWebSocket} instances.
	 *
	 * @author SimonIT */
	protected static class TeaWebSocketFactory implements WebSockets.WebSocketFactory {
		@Override
		public WebSocket newWebSocket(final String url) {
			return new TeaWebSocket(url);
		}
	}
}
