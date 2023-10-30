package han;

import java.util.InvalidPropertiesFormatException;
import java.util.Scanner;

import han.grpc.HandlerInitializer;
import han.kv.KVSingleton;
import han.kv.KVCmd;
import han.state.InitState;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MultiServerRunner {

    public static void main(String[] args) throws InvalidPropertiesFormatException {

        System.out.println("请输入本机id");
        Scanner scanner = new Scanner(System.in);
        int me = scanner.nextInt();
        scanner.nextLine();
        new Bootstrap().initLocalServer(me).initLogOperator().readClusterCnf().initHandler();
        System.out.println("running...input [quit] to quit");
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
                    toApply = new KVCmd(Integer.parseInt(scanner.nextLine()), scanner.nextLine());
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
