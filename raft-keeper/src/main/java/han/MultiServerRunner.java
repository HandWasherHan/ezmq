package han;

import java.util.Scanner;

import han.grpc.Sender;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MultiServerRunner {

    public static void main(String[] args) {
        Sender.init(true);
        System.out.println("running...input [quit] to quit");
        Scanner scanner = new Scanner(System.in);
        Server server = ServerSingleton.getServer();
        LogOperator logOperator;
        try {
            logOperator = new LogOperator("test" + server.getId() + ".log");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (String cmd = scanner.nextLine(); !cmd.equals("quit"); cmd = scanner.nextLine()) {
            server.getLogs().add(new Log(server.getTerm(), cmd));
            System.out.println(server.getLogs() + "commitIndex: " + server.getCommitIndex());
            if (Sender.send(MsgFactory.appendEntry(server), true)) {
                System.out.println("写入成功");
                server.setCommitIndex(server.commitIndex + 1);
                logOperator.write(server.getLogs().get(server.getLogs().size() - 1));
            } else {
                System.out.println("失败，请重试");
            }
        }
    }
}
