package han.state;

import com.google.protobuf.GeneratedMessageV3;

import han.grpc.MQService.Ack;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class InitState implements ServerState{
    @Override
    public void into() {
        throw new IllegalStateException("server未启动/未初始化");
    }

    @Override
    public void out() {
        throw new IllegalStateException("server未启动/未初始化");
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
