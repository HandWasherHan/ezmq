package han.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.grpc.MQService.Ack;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class LeaderState implements ServerState{
    static Logger logger = LogManager.getLogger(LeaderState.class);
    @Override
    public void into() {

    }

    @Override
    public void out() {

    }

    @Override
    public void idle() {

    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        return null;
    }

    @Override
    public void onAck(GeneratedMessageV3 ack) {
        logger.info("接收到响应:{}", ack);
        System.out.println(ack);
    }

    @Override
    public String toString() {
        return "LeaderState{}";
    }
}
