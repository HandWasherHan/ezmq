package han.state;

import static han.Constant.INIT_WAIT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.ServerVisitor;
import han.grpc.MQService.Ack;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class InitState implements ServerState{
    static final Logger logger = LogManager.getLogger(InitState.class);
    public InitState() {
        new Thread(() -> {
            logger.info("server状态为初始态，将于{}ms后自动转为follower", INIT_WAIT);
            try {
                Thread.sleep(INIT_WAIT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ServerVisitor.changeState(new FollowerState());
        }).start();
    }

    @Override
    public void into() {
        throw new IllegalStateException("server未启动/未初始化");
    }

    @Override
    public void out() {

    }

    @Override
    public void idle() {
        throw new IllegalStateException("server未启动/未初始化");
    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        throw new IllegalStateException("server未启动/未初始化");
    }

    @Override
    public void onAck(GeneratedMessageV3 ack) {
        throw new IllegalStateException("server未启动/未初始化");
    }

    @Override
    public String toString() {
        return "InitState{}";
    }
}
