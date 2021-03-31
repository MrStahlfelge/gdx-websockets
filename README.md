# libGDX Web Sockets

Fork of [czyzby's websockets](https://github.com/czyzby/gdx-lml/tree/master/websocket), which seem to be unmaintained.

See there for examples.

Default libGDX `Net` API provides only TCP sockets and HTTP requests. This library aims to add client-side web sockets support.
It works on all platforms targeted by libGDX.

## Dependencies
If you don't have already, add Jitpack to your repositories:

        maven { url "https://jitpack.io" }

`Gradle` dependency (for libGDX core project):
```
         compile "com.github.MrStahlfelge.gdx-websockets:core:$wsVersion"
```

GWT module:
```
         <inherits name='com.github.czyzby.websocket.GdxWebSocket' />
```

Desktop/Android/iOS:
```
         compile "com.github.MrStahlfelge.gdx-websockets:common:$wsVersion"
```

(based on [nv-websocket-client](https://github.com/TakahikoKawasaki/nv-websocket-client))

GWT:
```
        compile "com.github.MrStahlfelge.gdx-websockets:core:$wsVersion:sources"
        compile "com.github.MrStahlfelge.gdx-websockets:html:$wsVersion"
        compile "com.github.MrStahlfelge.gdx-websockets:html:$wsVersion:sources"
```

GWT module:
```
        <inherits name='com.github.czyzby.websocket.GdxWebSocketGwt' />
```

### Extensions

- [gdx-websocket-serialization](https://github.com/MrStahlfelge/gdx-websockets/tree/master/serialization): a custom serialization mechanism, not based on reflection. Alternative to JSON-based communication. More verbose, but gives you full control over (de)serialization process. Useful for performance-critical applications.

## Basic usage

### Initialization

Make sure to call `CommonWebSockets.initiate()` in DesktopLauncher/AndroidLauncher/IOSLauncher launchers before creating web sockets:
```
        // Initiating web sockets module - safe to call before creating application:
        CommonWebSockets.initiate();
        new LwjglApplication(new MyApplicationListener());
```

In HTMLLauncher, make sure to call `GwtWebSockets.initiate()` before creating web sockets:
```
        @Override
        public ApplicationListener createApplicationListener() {
            // Initiating web sockets module - safe to call before creating application listener:
            GwtWebSockets.initiate();
            return new MyApplicationListener();
        }
```

### Connecting to a server

```
        WebSocket socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
        socket.setSendGracefully(true);
        socket.addListener(new WebsocketListener() { ... });
        socket.connect();
```

## Changes

1.5 -> 1.6

- Added `AbstractWebSocketListener`, which handles object deserialization and logs errors. This is a solid base for your `WebSocketListener` implementation if don't use pure string-based communication. 
- Added `WebSocketHandler`, which extends `AbstractWebSocketListener` even further. Instead of dealing with raw `Object` types and having to determine packet type on your own, you can register a `Handler` to a specific packet class and it will be invoked each time a packet of the selected type is received.
- Added default `Serializer` implementation: `JsonSerializer`. Uses **LibGDX** `Json` API to serialize objects as strings.
- Added `WebSockets#DEFAULT_SERIALIZER`. Modify this field to automatically assign serializer of your choice to all new web socket instances.
- Added `Base64Serializer`. Uses **LibGDX** `Base64Coder` API to encode and decode the data to and from *BASE64*. Wraps around an existing serializer.
- Added custom serialization in [gdx-websocket-serialization](natives/serialization) library. `ManualSerializer` is an alternative to the default `JsonSerializer`.
- Added `WebSockets#closeGracefully(WebSocket)` null-safe utility method. Attempts to close the passed web socket and catches any thrown exceptions (their message is logged using `Gdx.app.debug` method). If passed web socket is null, it will be ignored. Useful for application disposing methods, when you don't exactly care if the web socket is not properly closed and *have to* continue disposing other native assets, even if `close()` call fails.
