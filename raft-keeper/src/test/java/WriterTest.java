import java.util.Scanner;

import org.junit.jupiter.api.Test;

import han.Log;
import han.LogOperator;
import han.MsgFactory;
import han.Server;
import han.ServerSingleton;
import han.grpc.Sender;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class WriterTest {

    @Test
    public void fun() throws IllegalAccessException {
        Sender.init(true, 1);
        System.out.println("running...input [quit] to quit");
        Scanner scanner = new Scanner(System.in);
        Server server = ServerSingleton.getServer();
        LogOperator logOperator = new LogOperator("test" + server.getId() + ".log");
        for (int i = 0; i < 10; i++) {
            String cmd = "hello world" + i;
            server.getLogs().add(new Log(server.getLogs().size(), cmd));
            System.out.println(server.getLogs());
            if (Sender.send(MsgFactory.mockLog(cmd))) {
                logOperator.write(server.getLogs().get(server.getLogs().size() - 1));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
