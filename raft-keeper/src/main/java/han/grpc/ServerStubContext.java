package han.grpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import han.MsgFactory;
import han.Server;
import han.ServerSingleton;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.RequestVote;
import han.grpc.RaftServiceGrpc.RaftServiceBlockingStub;
import han.grpc.MQService.Ack;
import han.state.LeaderState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ServerStubContext {
    static final Logger logger = LogManager.getLogger(ServerStubContext.class);
    Server server = ServerSingleton.getServer();
    ManagedChannel channel;
    RaftServiceBlockingStub stub;

    public ServerStubContext(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public ServerStubContext(ManagedChannelBuilder<?> builder) {
        this.channel = builder.build();
        this.stub = han.grpc.RaftServiceGrpc.newBlockingStub(channel);
    }

    public Ack send(AppendEntry appendEntry) {
        Ack ack = stub.sendAppendEntry(appendEntry);
        handle(ack);
        return ack;
    }

    public Ack send(RequestVote requestVote) {
        Ack ack = stub.sendRequestVote(requestVote);
        handle(ack);
        return ack;
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
        ServerStubContext serverStubContext = new ServerStubContext("localhost", 8848);
        serverStubContext.send(MsgFactory.requestVote(server));

    }
}
