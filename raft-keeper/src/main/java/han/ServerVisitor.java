package han;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import han.state.ServerState;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ServerVisitor {
    static final Logger logger = LogManager.getLogger(ServerVisitor.class);
    public static void changeState(Server server, ServerState state) {
        if (server.getState() == null) {
            logger.info("从无状态转变为{}", state);
            server.setState(state);
            state.into();
            return;
        }
        logger.info("从{}转变为{}", server.getState(), state);
        server.getState().out();
        server.setState(state);
        state.into();
    }

    public static void changeState(ServerState state) {
        Server server = ServerSingleton.getServer();
        changeState(server, state);

    }
}
