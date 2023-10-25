package han;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ServerSingleton {
    static Server server;
    synchronized static void init() {
        if (server == null) {
            server = new Server(1);
        }
    }


    public static Server getServer() {
        if (server == null) {
            init();
        }
        return server;
    }
}
