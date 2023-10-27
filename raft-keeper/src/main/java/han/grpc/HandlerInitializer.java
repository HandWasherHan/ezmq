package han.grpc;


/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class HandlerInitializer {
    static GrpcHandler grpcHandler;

    synchronized static void init(int port) {
        if (grpcHandler == null) {
            grpcHandler = new GrpcHandler(port);
            grpcHandler.run();
        }
    }

    public synchronized static void close() {
        if (grpcHandler != null) {
            grpcHandler.close();
        }
    }
}
