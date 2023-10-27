package han;

import java.util.Scanner;

import han.grpc.SenderListSingleton;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MultiServerRunner {

    public static void main(String[] args) {
        SenderListSingleton.init(true);
        System.out.println("running...input [quit] to quit");
        Scanner scanner = new Scanner(System.in);
        Server server = ServerSingleton.getServer();
        LogOperator logOperator;
        try {
            logOperator = new LogOperator("test" + server.getId() + ".log");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        while (!scanner.nextLine().equals("quit")) {
            String cmd = scanner.nextLine();
            server.getLogs().add(new Log(server.getLogs().size(), cmd));
            System.out.println(server.getLogs());
            if (SenderListSingleton.send(MsgFactory.mockLog(cmd))) {
                logOperator.write(server.getLogs().get(server.getLogs().size() - 1));
            }
        }
    }
}
