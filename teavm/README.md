# LibGDX Web Sockets for TeaVM
TeaVM natives for [LibGDX Web Sockets](../..). Make sure to call `TeaWebSockets.initiate()` before creating web sockets:
```
        public static void main (String[] args) {
            // Initiating web sockets module - safe to call before creating application:
            TeaWebSockets.initiate();
            new TeaApplication(new MyApplicationListener(), config);
        }
```

## Dependencies
`Gradle` dependency (for TeaVM LibGDX project):
```
        compile "com.github.czyzby:gdx-websocket-teavm:$libVersion.$gdxVersion"
        compile "com.github.czyzby:gdx-websocket-teavm:$libVersion.$gdxVersion:sources"
```
`$libVersion` is the current version of the library, usually following `MAJOR.MINOR` schema. `$gdxVersion` is the LibGDX version used to build (and required by) the library. You can check the current library version [here](http://search.maven.org/#search|ga|1|g%3A%22com.github.czyzby%22) - or you can use the [snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/czyzby/).
