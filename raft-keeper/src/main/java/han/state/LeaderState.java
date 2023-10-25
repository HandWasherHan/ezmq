package han.state;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.StateVisitor;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.Ack;
import han.Server;
import han.grpc.SenderListSingleton;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class LeaderState implements ServerState{
    static Logger logger = LogManager.getLogger(LeaderState.class);
    Server server;

    public LeaderState(Server server) {
        this.server = server;
    }

    ScheduledExecutorService scheduledExecutorService;
    @Override
    public void into() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        server.setNextIndex(new ArrayList<>());
        server.setMatchIndex(new ArrayList<>());
        idle();

    }

    @Override
    public void out() {
        scheduledExecutorService.shutdown();
    }

    @Override
    public void idle() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            SenderListSingleton.send(heartBeat());
        }, 0, 1, TimeUnit.SECONDS);
    }

    AppendEntry heartBeat() {
        int value = server.getLogs().size() - 1;
        return AppendEntry.newBuilder()
                .setLeaderId(0)
                .setCommitIndex(server.getCommitIndex())
                .setPrevLogIndex(value)
                .setPrevLogTerm(value == -1 ? 0 : server.getLogs().get(value).getTerm())
                .setTerm(server.getTerm())
                .build();
    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        if (msg instanceof AppendEntry) {
            AppendEntry appendEntry = (AppendEntry) msg;
            if (appendEntry.getTerm() > server.getTerm()) {
                StateVisitor.changeState(server, new FollowerState(server));

            }
        }
        return null;
    }

    /**
     * 处理异常ack
     * @param msg 接收到的ack消息
     */
    @Override
    public void onAck(GeneratedMessageV3 msg) {
        if (!(msg instanceof Ack)) {
            throw new IllegalArgumentException(msg + "不是一个ack");
        }
        logger.info("接收到响应:{}", msg);
        Ack ack = (Ack) msg;
        if (ack.getTerm() > server.getTerm()) {
            server.setTerm(ack.getTerm());
            StateVisitor.changeState(server, new FollowerState(server));
            return;
        }
    }

    @Override
    public String toString() {
        return "LeaderState{}";
    }
}
