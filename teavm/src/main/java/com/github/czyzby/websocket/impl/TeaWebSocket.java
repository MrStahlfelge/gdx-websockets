package com.github.czyzby.websocket.impl;

import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketException;
import com.github.czyzby.websocket.data.WebSocketState;
import org.teavm.jso.core.JSString;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.websocket.WebSocket;

/** Default web socket implementation for TeaVM applications. Implementation loosely based on
 * GwtWebSocket
 *
 * @author SimonIT */
public class TeaWebSocket extends AbstractWebSocket {

	protected WebSocket ws;

	public TeaWebSocket(String url) {
		super(url);
	}

	@Override
	public void sendBinary(byte[] packet) throws Exception {
		final ArrayBuffer arrayBuffer = new ArrayBuffer(packet.length);
		final Int8Array array = new Int8Array(arrayBuffer);
		array.set(packet);
		try {
			ws.send(arrayBuffer);
		} catch (final Throwable exception) {
			throw new WebSocketException(exception);
		}
	}

	@Override
	public void sendString(String packet) throws Exception {
		try {
			ws.send(packet);
		} catch (final Throwable exception) {
			throw new WebSocketException(exception);
		}
	}

	@Override
	public void connect() throws WebSocketException {
		if (isOpen() || isConnecting()) {
			close(WebSockets.ABNORMAL_AUTOMATIC_CLOSE_CODE);
		}
		try {
			open(super.getUrl());
		} catch (final Throwable exception) {
			throw new WebSocketException("Unable to open the web socket.", exception);
		}
	}

	/** @param url used to create the web socket. */
	protected void open(final String url) {
		if (url == null) {
			throw new WebSocketException("URL cannot be null.");
		}
		try {
			createWebSocket(url);
		} catch (final Throwable exception) {
			throw new WebSocketException("Unable to connect.", exception);
		}
	}

	/** @param url used to create the native web socket. */
	protected void createWebSocket(String url) {
		if (ws != null) {
			ws.close(WebSocketCloseCode.AWAY);
		}
		ws = new WebSocket(url);
		ws.onOpen(event -> {
			onOpen();
		});
		ws.setBinaryType("arraybuffer");
		ws.onClose(event -> {
			onClose(event.getCode(),  event.getReason());
		});
		ws.onError(error -> {
			onError(error.getType(), error.toString());
		});
		ws.onMessage(event -> {
			if (JSString.isInstance(event.getData())) {
				onMessage(((JSString) event.getData()).stringValue());
			} else {
				onMessage((ArrayBuffer) event.getData());
			}
		});
	}

	/** Invoked by native listener. */
	protected void onOpen() {
		postOpenEvent();
	}

	/** Invoked by native listener.
	 *
	 * @param closeCode see {@link WebSocketCloseCode}.
	 * @param reason optional closing reason. */
	protected void onClose(final int closeCode, final String reason) {
		postCloseEvent(closeCode, reason);
	}

	/** Invoked by native listener.
	 *
	 * @param type reported type of error.
	 * @param message full message of the error. */
	protected void onError(final String type, final String message) {
		postErrorEvent(
				new WebSocketException("An error occurred. Error type: " + type + ", error event message: " + message));
	}

	/** Invoked by native listener.
	 *
	 * @param arrayBuffer received binary frame. */
	protected void onMessage(final ArrayBuffer arrayBuffer) {
		if (arrayBuffer != null && arrayBuffer.getByteLength() > 0) {
			final byte[] message = toByteArray(arrayBuffer);
			if (message.length > 0) {
				postMessageEvent(message);
			}
		}
	}

	/** @param arrayBuffer will be converted to byte array.
	 * @return byte array with values stored in the buffer. */
	protected static byte[] toByteArray(final ArrayBuffer arrayBuffer) {
		final Int8Array array = new Int8Array(arrayBuffer);
		final int length = array.getByteLength();
		final byte[] byteArray = new byte[length];
		for (int index = 0; index < length; index++) {
			byteArray[index] = array.get(index);
		}
		return byteArray;
	}

	/** Invoked by native listener.
	 *
	 * @param message received text message. */
	protected void onMessage(final String message) {
		if (message != null && !message.isEmpty()) {
			postMessageEvent(message);
		}
	}

	@Override
	public WebSocketState getState() {
		try {
			return WebSocketState.getById(getStateId());
		} catch (final Throwable exception) {
			// Might be thrown if invalid state, for some reason.
			postErrorEvent(exception);
			return WebSocketState.CLOSED;
		}
	}

	public int getStateId() {
		if (ws == null) {
			return WebSocketState.CLOSED.getId();
		}
		return ws.getReadyState();
	}

	@Override
	public void close(int closeCode, String reason) throws WebSocketException {
		WebSocketCloseCode.checkIfAllowedInClient(closeCode);
		try {
			if (ws != null) {
				ws.close(closeCode, reason);
			}
		} catch (final Throwable exception) {
			throw new WebSocketException("Unable to close the web socket.", exception);
		}
	}

	@Override
	public boolean isSupported() {
		return WebSocket.isSupported();
	}

	@Override
	public String getUrl() {
		if (ws != null) {
			return ws.getUrl();
		}
		return super.getUrl();
	}
}
