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
import han.grpc.MQService;
import han.grpc.SenderListSingleton;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class CandidateState implements ServerState{
    static final Logger logger = LogManager.getLogger(CandidateState.class);
    ScheduledExecutorService scheduledExecutorService;
    @Override
    public void into() {
        Server server = ServerSingleton.getServer();
        logger.info("转成candidate状态");
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        server.setTerm(server.getTerm() + 1);
        idle();
        if(SenderListSingleton.send(MsgFactory.requestVote())) {
            StateVisitor.changeState(new LeaderState());
        }
    }

    @Override
    public void out() {
        scheduledExecutorService.shutdown();
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
    public MQService.Ack onReceive(GeneratedMessageV3 msg) {
        return null;
    }

    @Override
    public void onAck(GeneratedMessageV3 ack) {

    }
}
