package han.state;

import han.Server;
import han.msg.Ack;
import han.msg.AppendEntry;
import han.msg.Msg;
import han.msg.RequestVote;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class FollowerState implements ServerState{
    Server server;
    long lastTick;

    public FollowerState(Server server) {
        this.server = server;
    }


    @Override
    public void into() {
        server.setNextIndex(null);
        server.setMatchIndex(null);
    }

    @Override
    public void out() {

    }

    @Override
    public void idle() {

    }

    @Override
    public Ack onReceive(Msg msg) {
        if (msg instanceof AppendEntry) {
            // todo 写入日志
            return new Ack(server.getTerm(), false);
        } else if (msg instanceof RequestVote) {
            RequestVote rv = (RequestVote) msg;
            // todo 是否投票
            boolean canVote = false;
            // todo 检查candidate的日志是否为够新：1、lastLogIndex >= logs.size() 2、lastLogTerm >= term
            return new Ack(server.getTerm(), canVote);
        }
        return null;
    }

    /**
     * follower不会主动发请求，因而不会收到ack
     * @param ack
     * @return
     */
    @Override
    public Ack onAck(Ack ack) {
        return null;
    }
}
