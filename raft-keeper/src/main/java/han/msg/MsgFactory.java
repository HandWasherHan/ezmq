package han.msg;

import han.Server;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MsgFactory {
    public static AppendEntry send(Server leader) {
        AppendEntry appendEntry = new AppendEntry();
        appendEntry.leader = leader.getId();
        return appendEntry;
    }
    
    public static RequestVote requestVote(Server candidate) {
        RequestVote requestVote = new RequestVote();
        requestVote.term = candidate.getTerm();
        requestVote.candidateId = candidate.getId();
        requestVote.lastLogIndex = candidate.getLogs().size() - 1;
        requestVote.lastLogTerm = candidate.getLogs().get(requestVote.lastLogIndex).getTerm();
        return requestVote;
    }
}
