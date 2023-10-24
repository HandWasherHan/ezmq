package han.state;

import han.msg.Ack;
import han.msg.Msg;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public interface ServerState {
    void into();
    void out();
    void idle();
    Ack onReceive(Msg msg);

    Ack onAck(Ack ack);
}
