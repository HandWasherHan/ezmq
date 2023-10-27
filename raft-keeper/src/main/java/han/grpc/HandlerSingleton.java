package han.grpc;


/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class HandlerSingleton {
    static Handler handler;
   synchronized static void init(int port) {
        if (handler == null) {
            handler = new Handler(port);
            handler.run();
        }
    }

    public static Handler getHandler() {
        if (handler == null) {
            throw new IllegalStateException("请先初始化Handler");
        }
        return handler;
    }
}
