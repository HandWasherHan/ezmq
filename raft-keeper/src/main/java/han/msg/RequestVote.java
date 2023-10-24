package han.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class RequestVote implements Msg{
    int term;
    int candidateId;
    int lastLogIndex;
    int lastLogTerm;

}
