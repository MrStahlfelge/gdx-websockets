package com.github.czyzby.websocket.impl;

import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.data.WebSocketException;
import com.github.czyzby.websocket.data.WebSocketState;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.typedarrays.client.ArrayBufferNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;

import java.util.List;

/** Default web socket implementation for GWT applications. Implementation loosely based on
 * com.sksamuel.gwt.websockets.Websocket class - improved with binary data support and current state reporting.
 *
 * @author MJ
 * @see <a href="https://github.com/sksamuel/gwt-websockets">gwt-websockets</a> */
public class GwtWebSocket extends AbstractWebSocket {
    public GwtWebSocket(final String url, final List<String> protocols) {
        super(url, protocols);
    }

    /** @return true if web sockets are supported by the browser. */
    public static native boolean areWebSocketsSupported() /*-{
                                                          return ("WebSocket" in window);
                                                          }-*/;

    @Override
    public void connect() throws WebSocketException {
        if (isOpen() || isConnecting()) {
            close(WebSockets.ABNORMAL_AUTOMATIC_CLOSE_CODE);
        }
        try {
            open(super.getUrl(), super.getProtocols());
        } catch (final Throwable exception) {
            throw new WebSocketException("Unable to open the web socket.", exception);
        }
    }

    /** @param url used to create the web socket. */
    protected void open(final String url, final List<String> protocols) {
        if (url == null) {
            throw new WebSocketException("URL cannot be null.");
        }
        JsArrayString protocolsArray = createProtocols(protocols);
        try {
            createWebSocket(url, protocolsArray);
        } catch (final Throwable exception) {
            throw new WebSocketException("Unable to connect.", exception);
        }
    }

    private JsArrayString createProtocols(List<String> protocols) {
        if (protocols == null || protocols.isEmpty()) return null;

        JsArrayString result = JavaScriptObject.createArray().cast();
        protocols.forEach(result::push);
        return result;
    }

    /** @param url used to create the native web socket. */
    protected native void createWebSocket(String url, JsArrayString protocols)/*-{
                                                     var self = this;
                                                     if(self.ws) {
                                                     self.ws.close(1001);
                                                     }
                                                     self.ws = new WebSocket(url, protocols);
                                                     self.ws.onopen = function() { self.@com.github.czyzby.websocket.impl.GwtWebSocket::onOpen()(); };
                                                     self.ws.binaryType = 'arraybuffer';
                                                     self.ws.onclose = function(event) { self.@com.github.czyzby.websocket.impl.GwtWebSocket::onClose(ILjava/lang/String;)(event.code, event.reason); };
                                                     self.ws.onerror = function(error) { self.@com.github.czyzby.websocket.impl.GwtWebSocket::onError(Ljava/lang/String;Ljava/lang/String;)(error.type,error.toString()); };
                                                     self.ws.onmessage = function(msg) {
                                                     if (typeof(msg.data) == 'string' ) {
                                                     self.@com.github.czyzby.websocket.impl.GwtWebSocket::onMessage(Ljava/lang/String;)(msg.data);
                                                     }else{
                                                     self.@com.github.czyzby.websocket.impl.GwtWebSocket::onMessage(Lcom/google/gwt/typedarrays/shared/ArrayBuffer;)(msg.data);
                                                     }
                                                     }
                                                     }-*/;

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
        if (arrayBuffer != null && arrayBuffer.byteLength() > 0) {
            final byte[] message = toByteArray(arrayBuffer);
            if (message.length > 0) {
                postMessageEvent(message);
            }
        }
    }

    /** @param arrayBuffer will be converted to byte array.
     * @return byte array with values stored in the buffer. */
    protected static byte[] toByteArray(final ArrayBuffer arrayBuffer) {
        final Int8Array array = Int8ArrayNative.create(arrayBuffer);
        final int length = array.byteLength();
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
        if (message != null && message.length() > 0) {
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

    /** @return current ready state of the socket.
     * @see WebSocketState */
    protected native int getStateId()/*-{
                                     if(this.ws) {
                                     return this.ws.readyState;
                                     }
                                     return 3;
                                     }-*/;

    @Override
    public void close(final int closeCode, final String reason) throws WebSocketException {
        WebSocketCloseCode.checkIfAllowedInClient(closeCode);
        try {
            nativeClose(closeCode, reason);
        } catch (final Throwable exception) {
            throw new WebSocketException("Unable to close the web socket.", exception);
        }
    }

    /** Closes the native web socket.
     *
     * @param code see {@link WebSocketCloseCode}.
     * @param reason optional closing reason. */
    protected native void nativeClose(int code, String reason)/*-{
                                                        if(this.ws){
                                                        this.ws.close(code,reason);
                                                        }
                                                        }-*/;

    @Override
    public void sendBinary(final byte[] message) {
        final ArrayBuffer arrayBuffer = ArrayBufferNative.create(message.length);
        final Int8Array array = Int8ArrayNative.create(arrayBuffer);
        array.set(message);
        try {
            sendArrayBuffer(arrayBuffer);
        } catch (final Throwable exception) {
            throw new WebSocketException(exception);
        }
    }

    /** @param message will be sent using native web socket. */
    protected native void sendArrayBuffer(ArrayBuffer message)/*-{
                                                              if(this.ws) {
                                                              this.ws.send(message);
                                                              }
                                                              }-*/;

    @Override
    public void sendString(final String message) {
        try {
            sendText(message);
        } catch (final Throwable exception) {
            throw new WebSocketException(exception);
        }
    }

    /** @param message will be sent using native web socket. */
    protected native void sendText(String message)/*-{
                                                  if(this.ws) {
                                                  this.ws.send(message);
                                                  }
                                                  }-*/;

    @Override
    public boolean isSupported() {
        return areWebSocketsSupported();
    }

    @Override
    public String getUrl() {
        final String url = getWebSocketUrl();
        return url == null ? super.getUrl() : url;
    }

    /** @return current URL stored by native web socket or null. */
    public native String getWebSocketUrl()/*-{
                                          if(this.ws) {
                                          return this.ws.url;
                                          }
                                          return null;
                                          }-*/;
}
