package han;

import han.Server;
import han.grpc.MQService.RequestVote;
import han.grpc.MQService.Ack;
import han.grpc.MQService.AppendEntry;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MsgFactory {

    public static AppendEntry send(Server leader) {
        return AppendEntry.newBuilder()
                .setLeaderId(leader.getId())
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

}
