package han;

import java.util.ArrayList;
import java.util.List;

import han.grpc.Sender;
import han.state.ServerState;
import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class Server {
    // meta data
    int id;
    Integer leaderId;

    // persistent
    int term;
    Integer voteFor;
    List<Log> logs;

    // volatile
    int commitIndex;
    int lastApplied;

    // volatile, leader only
    List<Integer> nextIndex;
    List<Integer> matchIndex;

    ServerState state;

    public Server(int id) {
        this.id = id;
        // todo 从日志文件中恢复
        this.logs = new ArrayList<>();
    }

    /**
     * client向server中写入数据
     * @param cmd 格式化后的命令
     * @return 0:成功, -1:失败, 其他正数:leaderId
     */
    public int write(String cmd) {
        if (leaderId != id) {
            return leaderId;
        }
        Server server = this;
        LogOperator logOperator;
        try {
            logOperator = new LogOperator("test" + server.getId() + ".log");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        server.getLogs().add(new Log(server.getTerm(), cmd));
        System.out.println(server.getLogs() + "commitIndex: " + server.getCommitIndex());
        if (Sender.send(MsgFactory.appendEntry(server), true)) {
            server.setCommitIndex(server.commitIndex + 1);
            logOperator.write(server.getLogs().get(server.getLogs().size() - 1));
        } else {
            return -1;
        }
        return 0;
    }


}
