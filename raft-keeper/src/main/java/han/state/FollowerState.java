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

import han.LogOperator;
import han.MsgFactory;
import han.Server;
import han.ServerSingleton;
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
    long lastTick;

    public FollowerState() {
    }


    @Override
    public void into() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        idle();
        Server server = ServerSingleton.getServer();
        server.setVoteFor(null);
        server.setNextIndex(null);
        server.setMatchIndex(null);
    }

    @Override
    public void out() {
        scheduledExecutorService.shutdownNow();
    }

    @Override
    public void idle() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            logger.debug("超时检测中...");
            if (System.currentTimeMillis() - lastTick > HEART_BEAT_INTERVAL * 2 + new Random().nextInt(RANDOM_BOUND)) {
                logger.info("超时，触发选举");
                StateVisitor.changeState(ServerSingleton.getServer(), new CandidateState());
            }
        }, HEART_BEAT_INTERVAL * 10,  HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS);

    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        logger.debug("接收到消息:\n{}", msg);
        lastTick = System.currentTimeMillis();
        Server server = ServerSingleton.getServer();
        LogOperator logOperator;
        try {
            logOperator = new LogOperator("test" + server.getId() + ".log");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (msg instanceof AppendEntry) {
            AppendEntry appendEntry = (AppendEntry) msg;
            while (!appendEntry.getEntryList().isEmpty() && server.getCommitIndex() < appendEntry.getCommitIndex()) {
                logOperator.write(MsgFactory.log(appendEntry.getEntry(0)));
                server.setCommitIndex(server.getCommitIndex() + 1);
                // todo apply
            }
            return Ack.newBuilder().setTerm(server.getTerm()).setSuccess(true).build();
        } else if (msg instanceof RequestVote) {
            RequestVote rv = (RequestVote) msg;
            boolean canVote = canVote(rv, server);
            if (canVote) {
                server.setVoteFor(rv.getCandidateId());
            }
            logger.info("收到来自{}的选举请求, canVote:{}", rv.getCandidateId(), canVote);
            return Ack.newBuilder().setTerm(server.getTerm()).setSuccess(canVote).build();
        }
        return null;
    }

    boolean canVote(RequestVote rv, Server server) {
        if (server.getVoteFor() != null) {
            return false;
        }
        if (rv.getTerm() < server.getTerm()) {
            return false;
        }
        if (server.getLogs().isEmpty()) {
            return true;
        }
        int lastIndex = server.getLogs().size();
        if (rv.getLastLogIndex() < lastIndex) {
            return false;
        }
        boolean remoteTermExpired = rv.getLastLogTerm() < server.getLogs().get(lastIndex - 1).getTerm();
        return !remoteTermExpired;
    }

    /**
     * follower不会主动发请求，因而不会收到ack
     */
    @Override
    public void onAck(GeneratedMessageV3 ack) {
    }
}
