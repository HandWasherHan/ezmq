package han.grpc;

import java.io.IOException;

import com.google.protobuf.GeneratedMessageV3;

import han.Server;
import han.grpc.RaftServiceGrpc.RaftServiceImplBase;
import han.grpc.MQService.Ack;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.RequestVote;
import han.state.FollowerState;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * 处理来自sender的请求，包括
 * <li>AppendEntry</li>
 * <li>RequestVote</li>
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class Handler {

    class RaftHandler extends RaftServiceImplBase {
        Server server;
        public RaftHandler() {
        }

        public RaftHandler(Server server) {
            this.server = server;
        }

        @Override
        public void sendAppendEntry(AppendEntry request, StreamObserver<Ack> responseObserver) {
            handle(request, responseObserver);
        }

        @Override
        public void sendRequestVote(RequestVote request, StreamObserver<Ack> responseObserver) {
            handle(request, responseObserver);
        }
        void assertInitialized() {
            if (server == null) {
                throw new IllegalStateException("handler未初始化");
            }
        }

        /**
         * 由server的state代理
         */
        void handle(GeneratedMessageV3 request, StreamObserver<Ack> responseObserver) {
            assertInitialized();
            Ack ack = server.getState().onReceive(request);
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
        }
    }

    public void run(Server server, int port) {
        try {
            io.grpc.Server grpcServer = ServerBuilder.forPort(port).addService(new RaftHandler(server)).build();
            grpcServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public static void main(String[] args) throws InterruptedException {
        Server server = new Server(2);
        server.setState(new FollowerState(server));
        new Handler().run(server, 8848);
        Thread.sleep(10000000);
    }
}
