package han;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ServerSingleton {
    static Server server;

    public synchronized static void init(int id) {
        if (server == null) {
            server = new Server(id);
        }
    }

    public static Server getServer() {
        return server;
    }
}
