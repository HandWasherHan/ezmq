package handler.event;

import java.io.Serializable;

import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class VoteEvent implements Serializable {
    // 正数用于当选后，通知其它follower自己成为了leader
    public static final int CANVASS = -1; // 参选
    public static final int VOTE = -2; // 投票
//    public static final int REJECT = -3; // 投过票了，拒绝其它的拉票请求
    private int type;

    public VoteEvent(int type) {
        this.type = type;
    }

    public static VoteEvent canvass() {
        return new VoteEvent(CANVASS);
    }

//    public static VoteEvent vote() {
//        return new VoteEvent(VOTE);
//    }
//
//    public static VoteEvent reject() {
//        return new VoteEvent(REJECT);
//    }

    /**
     *
     * @param id 当选者id
     * @return
     */
    public static VoteEvent elected(int id) {
        return new VoteEvent(id);
    }
}
