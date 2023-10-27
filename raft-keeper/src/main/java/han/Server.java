package han;

import java.util.ArrayList;
import java.util.List;

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
    int serverCnt;

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
     * @param msg 格式化后的命令
     * @return 0:成功, -1:失败, 其他正数:leaderId
     */
    public int write(String msg) {
        if (leaderId != id) {
            return leaderId;
        }
        return 0;
    }


}
