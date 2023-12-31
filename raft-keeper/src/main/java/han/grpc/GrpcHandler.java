package han.grpc;

import java.io.IOException;

import com.google.protobuf.GeneratedMessageV3;

import han.ServerSingleton;
import han.grpc.RaftServiceGrpc.RaftServiceImplBase;
import han.grpc.MQService.Ack;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.RequestVote;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * 处理请求，包括
 * <li>AppendEntry</li>
 * <li>RequestVote</li>
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class GrpcHandler {
    int port;
    io.grpc.Server grpcServer;

    public GrpcHandler(int port) {
        this.port = port;
    }

    public void close() {
        grpcServer.shutdownNow();
    }

    static class RaftHandler extends RaftServiceImplBase {

        @Override
        public void sendAppendEntry(AppendEntry request, StreamObserver<Ack> responseObserver) {
            handle(request, responseObserver);
        }

        @Override
        public void sendRequestVote(RequestVote request, StreamObserver<Ack> responseObserver) {
            handle(request, responseObserver);
        }


        /**
         * 由server的state代理
         */
        void handle(GeneratedMessageV3 request, StreamObserver<Ack> responseObserver) {
            Ack ack = ServerSingleton.getServer().getState().onReceive(request);
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
        }
    }

    public void run() {
        try {
            grpcServer = ServerBuilder.forPort(port).addService(new RaftHandler()).build();
            grpcServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
