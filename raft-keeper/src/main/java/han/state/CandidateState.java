package han.state;

import static han.Constant.VOTE_TIME;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.MsgFactory;
import han.Server;
import han.ServerSingleton;
import han.StateVisitor;
import han.grpc.Sender;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.Ack;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class CandidateState implements ServerState{
    static final Logger logger = LogManager.getLogger(CandidateState.class);
    ScheduledExecutorService scheduledExecutorService;
    @Override
    public void into() {
        logger.info("转成candidate状态");
        Server server = ServerSingleton.getServer();
        server.setTerm(server.getTerm() + 1);
        server.setVoteFor(server.getId());
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        idle();
        scheduledExecutorService.execute(() -> {
            if(Sender.send(MsgFactory.requestVote())) {
                logger.info("选举成功，我是新的leader，任期号为{}", ServerSingleton.getServer().getTerm());
                StateVisitor.changeState(new LeaderState());
            }
        });

    }

    @Override
    public void out() {
        scheduledExecutorService.shutdownNow();
    }

    @Override
    public void idle() {
        Server server = ServerSingleton.getServer();
        scheduledExecutorService.schedule(() -> {
            logger.info("选举超时失败，重新选举");
            StateVisitor.changeState(server, new FollowerState());
        }, VOTE_TIME, TimeUnit.SECONDS);
    }

    @Override
    public Ack onReceive(GeneratedMessageV3 msg) {
        Server server = ServerSingleton.getServer();
        if (msg instanceof AppendEntry) {
            AppendEntry appendEntry = (AppendEntry) msg;
            if (appendEntry.getTerm() >= server.getTerm()) {
                StateVisitor.changeState(new FollowerState());
                server.setLeaderId(appendEntry.getLeaderId());
            }
        }
        return null;
    }

    @Override
    public void onAck(GeneratedMessageV3 ack) {

    }
}
