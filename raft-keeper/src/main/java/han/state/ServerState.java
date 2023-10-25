package han.state;

import com.google.protobuf.GeneratedMessageV3;
import han.grpc.MQService.Ack;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public interface ServerState {
    void into();
    void out();
    void idle();
    Ack onReceive(GeneratedMessageV3 msg);
    void onAck(GeneratedMessageV3 ack);
}
