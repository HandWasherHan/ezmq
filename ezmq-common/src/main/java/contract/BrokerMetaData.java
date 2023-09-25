package contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrokerMetaData implements Serializable {
    private Integer leaderId;
    private String leaderHostAddr;
    private int id;
    private int term;
    private List<String> members = new ArrayList<>();

    public boolean isLeader() {
        if (leaderId == null && leaderHostAddr != null) {
            throw new IllegalStateException("错误的metadata");
        }
        return leaderId == null;
    }

    public void update(BrokerMetaData metaData) {
        if (metaData.getMembers() != null) {
            this.members = metaData.getMembers();
        }
        if (metaData.getTerm() != 0) {
            this.term = metaData.getTerm();
        }
        if (metaData.getId() != 0) {
            this.id = metaData.getId();
        }
        if (metaData.getLeaderHostAddr() != null) {
            this.leaderHostAddr = metaData.getLeaderHostAddr();
        }
        if (metaData.getLeaderId() != null) {
            this.leaderId = metaData.getLeaderId();
        }
    }

}
