package han.state;

import static han.Constant.VOTE_TIME;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3;

import han.Server;
import han.StateVisitor;
import han.grpc.MQService;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class CandidateState implements ServerState{
    static final Logger logger = LogManager.getLogger(CandidateState.class);
    Server server;
    ScheduledExecutorService scheduledExecutorService;
    @Override
    public void into() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        server.setTerm(server.getTerm() + 1);
        idle();
    }

    @Override
    public void out() {
        scheduledExecutorService.shutdown();
    }

    @Override
    public void idle() {
        scheduledExecutorService.schedule(() -> {
            StateVisitor.changeState(server, new CandidateState());
        }, VOTE_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public MQService.Ack onReceive(GeneratedMessageV3 msg) {
        return null;
    }

    @Override
    public void onAck(GeneratedMessageV3 ack) {

    }
}
