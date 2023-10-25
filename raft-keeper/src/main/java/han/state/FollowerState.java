package han.state;

import static han.Constant.HEART_BEAT_INTERVAL;
import static han.Constant.RANDOM_BOUND;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.Server;
import han.StateVisitor;
import han.grpc.MQService.Ack;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.RequestVote;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class FollowerState implements ServerState{
    static Logger logger = LogManager.getLogger(FollowerState.class);
    ScheduledExecutorService scheduledExecutorService;
    Server server;
    long lastTick;

    public FollowerState(Server server) {
        this.server = server;
    }


    @Override
    public void into() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        server.setNextIndex(null);
        server.setMatchIndex(null);
    }

    @Override
    public void out() {
        scheduledExecutorService.shutdown();
    }

    @Override
    public void idle() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastTick > HEART_BEAT_INTERVAL * 2 + new Random().nextInt(RANDOM_BOUND)) {
                StateVisitor.changeState(server, new CandidateState());
            }
        }, 0,  HEART_BEAT_INTERVAL, TimeUnit.SECONDS);

    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        logger.info("接收到消息:{}", msg);
        lastTick = System.currentTimeMillis();
        if (msg instanceof AppendEntry) {
            // todo 写入日志
            return Ack.newBuilder().setTerm(server.getTerm()).setSuccess(true).build();
        } else if (msg instanceof RequestVote) {
            RequestVote rv = (RequestVote) msg;
            // todo 是否投票
            boolean canVote = false;
            // todo 检查candidate的日志是否为够新：1、lastLogIndex >= logs.size() 2、lastLogTerm >= term
            return Ack.newBuilder().setTerm(server.getTerm()).setSuccess(canVote).build();
        }
        return null;
    }

    /**
     * follower不会主动发请求，因而不会收到ack
     * @param ack
     * @return
     */
    @Override
    public void onAck(GeneratedMessageV3 ack) {
    }
}
