package han.grpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import han.MsgFactory;
import han.Server;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.RequestVote;
import han.grpc.MQService.Ack;
import han.state.LeaderState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class Sender {
    static final Logger logger = LogManager.getLogger(Sender.class);
    Server server;
    ManagedChannel channel;
    han.grpc.RaftServiceGrpc.RaftServiceBlockingStub stub;

    public Sender(String host, int port, Server server) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
        this.server = server;
    }

    public Sender(ManagedChannelBuilder<?> builder) {
        this.channel = builder.build();
        this.stub = han.grpc.RaftServiceGrpc.newBlockingStub(channel);
    }

    public void send(AppendEntry appendEntry) {
        Ack ack = stub.sendAppendEntry(appendEntry);
        // todo       处理ack
        handle(ack);
    }

    public void send(RequestVote requestVote) {
        Ack ack = stub.sendRequestVote(requestVote);
        // todo       处理ack
        handle(ack);
    }

    void assertInitialized() {
        if (server == null) {
            throw new IllegalStateException("handler未初始化");
        }
    }

    /**
     * 由server的state代理
     */
    void handle(Ack ack) {
        assertInitialized();
        server.getState().onAck(ack);
    }

    public static void main(String[] args) {
        logger.info("启动");
        Server server = new Server(1);
        server.setState(new LeaderState());
        Sender sender = new Sender("localhost", 8848, server);
        sender.send(MsgFactory.requestVote(server));

    }

}
