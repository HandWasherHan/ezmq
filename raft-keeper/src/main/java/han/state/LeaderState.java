package han.state;

import static han.Constant.HEART_BEAT_INTERVAL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.Cmd;
import han.Constant;
import han.Log;
import han.ServerSingleton;
import han.ServerVisitor;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.Ack;
import han.Server;
import han.grpc.Sender;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class LeaderState implements ServerState{
    static Logger logger = LogManager.getLogger(LeaderState.class);

    ScheduledExecutorService scheduledExecutorService;

    @Override
    public void into() {
        Server server = ServerSingleton.getServer();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        idle();
        server.setNextIndex(new ArrayList<>(Constant.clusterSize));
        server.setMatchIndex(new ArrayList<>(Constant.clusterSize));
        for (int i = 0; i < Constant.clusterSize; i++) {
            server.getNextIndex().add(0);
            server.getMatchIndex().add(0);
        }
        reboot();
    }

    @Override
    public void out() {
        scheduledExecutorService.shutdownNow();
    }

    @Override
    public void idle() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long time = System.currentTimeMillis();
            Sender.send(heartBeat());
            logger.debug("发送心跳消息完成，耗时{}ms", System.currentTimeMillis() - time);
        }, 0, HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    AppendEntry heartBeat() {
        Server server = ServerSingleton.getServer();
        int value = server.getLogs().size() - 1;
        return AppendEntry.newBuilder()
                .setLeaderId(server.getId())
                .setCommitIndex(server.getCommitIndex())
                .setPrevLogIndex(value)
                .setPrevLogTerm(value == -1 ? 0 : server.getLogs().get(value).getTerm())
                .setTerm(server.getTerm())
                .build();
    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        Server server = ServerSingleton.getServer();
        if (msg instanceof AppendEntry) {
            AppendEntry appendEntry = (AppendEntry) msg;
            if (appendEntry.getTerm() > server.getTerm()) {
                ServerVisitor.changeState(server, new FollowerState());
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
        Server server = ServerSingleton.getServer();
        if (!(msg instanceof Ack)) {
            throw new IllegalArgumentException(msg + "不是一个ack");
        }
        logger.debug("接收到响应:{}", msg);
        Ack ack = (Ack) msg;
        if (ack.getTerm() > server.getTerm()) {
            server.setTerm(ack.getTerm());
            ServerVisitor.changeState(server, new FollowerState());
        }
    }

    synchronized void reboot() {
        Server server = ServerSingleton.getServer();
        List<Log> logs = server.getLogs();
        int commitIndex = server.getCommitIndex();
        while (commitIndex < logs.size() - 1) {
            String cmd = logs.get(commitIndex + 1).getCmd();
            Cmd.decode(cmd).apply();
            commitIndex++;
        }
        server.setCommitIndex(commitIndex);
    }

    @Override
    public String toString() {
        return "LeaderState{}";
    }
}
