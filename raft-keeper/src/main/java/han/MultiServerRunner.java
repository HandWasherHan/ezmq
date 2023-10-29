package han;

import java.util.Scanner;

import han.grpc.HandlerInitializer;
import han.grpc.Sender;
import han.mock.KVSingleton;
import han.mock.MockCmd;
import han.state.InitState;

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
        for (String cmd = scanner.nextLine(); !cmd.equals("quit"); cmd = scanner.nextLine()) {
            Cmd toApply = null;
            switch (cmd) {
                case "防idea误报":
                    break;
                case "logs":
                    System.out.println(server.getLogs());
                    System.out.println("commit: " + server.getCommitIndex() + ", last applied: " + server.getLastApplied());
                    continue;
                case "kv":
                    System.out.println(KVSingleton.map);
                    continue;
                case "set":
                    toApply = new MockCmd(Integer.parseInt(scanner.nextLine()), scanner.nextLine());
                    cmd = Cmd.encode(toApply);
                    break;
                default:
                    System.out.println("无效命令");
                    continue;
            }
            int write = server.write(cmd);
            switch (write) {
                case -1:
                    System.out.println("失败，请重试");
                    break;
                case 0:
                    System.out.println("写入成功");
                    // idea误报
                    if (toApply != null) {
                        toApply.apply();
                        server.setLastApplied(server.lastApplied + 1);
                    }
                    break;
                default:
                    System.out.println("我不是leader，请向id为" + write + "的server发送消息");
            }

        }
        HandlerInitializer.close();
        // to close the thread pools
        ServerVisitor.changeState(new InitState());
        System.out.println("bye~");
    }
}
