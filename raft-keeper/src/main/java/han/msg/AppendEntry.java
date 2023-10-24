package han.msg;

import han.Log;
import han.Server;

import java.util.List;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class AppendEntry implements Msg{
    int leader;
    int term;
    int prevLogIndex;
    int prevLogTerm;
    List<Log> entries;
    int leaderCommit;



}
