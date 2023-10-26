package han;

import han.state.FollowerState;
import han.state.InitState;

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
        server.setState(new InitState());


    }

    public static Server getServer() {
        return server;
    }
}
