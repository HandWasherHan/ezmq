package han;

import han.state.ServerState;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class StateVisitor {
    public static void changeState(Server server, ServerState state) {
        server.getState().out();
        server.setState(state);
        state.into();
    }
}
