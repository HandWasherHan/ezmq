package han;

import han.grpc.MQService;
import han.grpc.MQService.RequestVote;
import han.grpc.MQService.AppendEntry;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MsgFactory {

    public static AppendEntry appendEntry(Server leader) {
        return AppendEntry.newBuilder()
                .setLeaderId(leader.getId())
                .build();
    }

    public static AppendEntry appendEntry(int target) {
        Server server = ServerSingleton.getServer();
        Integer index = server.getNextIndex().get(target);
        return AppendEntry.newBuilder()
                .setLeaderId(server.getId())
                .setEntry(0, log(server.getLogs().get(index)))
                .build();
    }

    /**
     * lastIndex：上一条日志的下标。用的是int，不能为空，用-1代表没有上一条日志的情况
     * @param candidate
     * @return
     */
    public static RequestVote requestVote(Server candidate) {
        int lastIndex = candidate.getLogs().size() - 1;
        try {
            return RequestVote.newBuilder()
                    .setTerm(candidate.getTerm())
                    .setCandidateId(candidate.getId())
                    .setLastLogIndex(lastIndex)
                    .setLastLogTerm(lastIndex == -1 ? 0 : candidate.getLogs().get(lastIndex).getTerm())
                    .build();
        } catch (NullPointerException npe) {
            // 不处理
            throw npe;
        }
    }

    public static RequestVote requestVote() {
        return requestVote(ServerSingleton.getServer());
    }

    public static MQService.Log log(Log log) {
        return MQService.Log.newBuilder().setTerm(log.term).setCmd(log.cmd).build();
    }

    public static Log log(MQService.Log log) {
        return new Log(log.getTerm(), log.getCmd());
    }

    public static AppendEntry mockLog(String cmd) {
        Server server = ServerSingleton.getServer();
        Integer index = 0;
        return AppendEntry.newBuilder()
                .setLeaderId(server.getId())
                .setCommitIndex(2)
                .addEntry(0, log(new Log(1, cmd)))
                .build();
    }

}
