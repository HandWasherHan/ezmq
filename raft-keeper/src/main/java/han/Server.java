package han;

import java.util.List;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class Server {
    // meta data
    int id;
    boolean isLeader;

    // persistent
    int term;
    int voteFor;
    List<Log> logs;

    // volatile
    int commitIndex;
    int lastApplied;

    // volatile, leader only
    List<Integer> nextIndex;
    List<Integer> matchIndex;

    public Server(int id) {
        this.id = id;
    }


}
