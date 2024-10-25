package com.github.czyzby.websocket.impl;

import com.android.org.conscrypt.OpenSSLSocketFactoryImpl;
import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketException;
import com.github.czyzby.websocket.data.WebSocketState;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/** Default web socket implementation for desktop and mobile platforms.
 *
 * @author MJ */
public class NvWebSocket extends AbstractWebSocket {
    private final WebSocketFactory webSocketFactory = new WebSocketFactory();
    private WebSocket webSocket;

    private void setSNIForSocket (Socket socket) {
        if (hostnameForSni != null) {
            if (socket instanceof OpenSSLSocketImpl) {
                ((OpenSSLSocketImpl) socket).setHostname(hostnameForSni);
            }
        }
    }

    public NvWebSocket(final String url) {
        super(url);
        webSocketFactory.setVerifyHostname(verifyHostname);
        webSocketFactory.setSSLSocketFactory(new OpenSSLSocketFactoryImpl() {

            @Override
            public Socket createSocket(InetAddress host, int port) throws IOException {
                Socket socket = super.createSocket(host, port);
                setSNIForSocket(socket);
                return socket;
            }

            @Override
            public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
                Socket socket = super.createSocket(s, host, port, autoClose);
                setSNIForSocket(socket);
                return socket;
            }

            @Override
            public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
                Socket socket = super.createSocket(host, port, localHost, localPort);
                setSNIForSocket(socket);

                return socket;
            }

            @Override
            public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
                Socket socket = super.createSocket(address, port, localAddress, localPort);
                setSNIForSocket(socket);
                return socket;
            }

            @Override
            public Socket createSocket() throws IOException {
                Socket socket = super.createSocket();
                setSNIForSocket(socket);
                return socket;
            }

            @Override
            public Socket createSocket(String host, int port) throws IOException {
                Socket socket = super.createSocket(host, port);
                setSNIForSocket(socket);
                return socket;
            }
        });
    }

    @Override
    public void connect() throws WebSocketException {
        try {
            dispose();
            final WebSocket currentWebSocket = webSocket = webSocketFactory.createSocket(getUrl());

            webSocket.setPingInterval(5000);
            webSocket.setPongInterval(5000);

            currentWebSocket.addListener(new NvWebSocketListener(this));
            currentWebSocket.connectAsynchronously();
        } catch (final Throwable exception) {
            throw new WebSocketException("Unable to connect.", exception);
        }
    }

    @Override
    public void setUseTcpNoDelay(boolean useTcpNoDelay) {
        super.setUseTcpNoDelay(useTcpNoDelay);
        if (webSocket != null && webSocket.getSocket() != null) {
            try {
                webSocket.getSocket().setTcpNoDelay(useTcpNoDelay);
            } catch (SocketException ignored) {
            }
        }
    }

    /** Removes current web socket instance. */
    protected void dispose() {
        final WebSocket currentWebSocket = webSocket;
        if (currentWebSocket != null && currentWebSocket.isOpen()) {
            try {
                currentWebSocket.disconnect(WebSockets.ABNORMAL_AUTOMATIC_CLOSE_CODE);
            } catch (final Exception exception) {
                postErrorEvent(exception);
            }
        }
    }

    @Override
    public WebSocketState getState() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket == null ? WebSocketState.CLOSED : convertState(currentWebSocket.getState());
    }

    private static WebSocketState convertState(final com.neovisionaries.ws.client.WebSocketState state) {
        switch (state) {
            case CLOSED:
            case CREATED:
                return WebSocketState.CLOSED;
            case CLOSING:
                return WebSocketState.CLOSING;
            case CONNECTING:
                return WebSocketState.CONNECTING;
            case OPEN:
                return WebSocketState.OPEN;
        }
        return WebSocketState.CLOSED;
    }

    @Override
    public boolean isSecure() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket != null && "wss".equalsIgnoreCase(currentWebSocket.getURI().getScheme());
    }

    @Override
    public boolean isOpen() {
        final WebSocket currentWebSocket = webSocket;
        return currentWebSocket != null && currentWebSocket.isOpen();
    }

    @Override
    public void close(final int closeCode, final String reason) throws WebSocketException {
        WebSocketCloseCode.checkIfAllowedInClient(closeCode);
        final WebSocket currentWebSocket = webSocket;
        if (currentWebSocket != null) {
            try {
                currentWebSocket.disconnect(closeCode, reason);
            } catch (final Throwable exception) {
                throw new WebSocketException("Unable to close the web socket.", exception);
            }
        }
    }

    @Override
    protected void sendBinary(final byte[] packet) throws Exception {
        webSocket.sendBinary(packet);
    }

    @Override
    protected void sendString(final String packet) throws Exception {
        webSocket.sendText(packet);
    }
}
