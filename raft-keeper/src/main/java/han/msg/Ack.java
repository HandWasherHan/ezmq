package han.msg;

import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class Ack {
    int term;
    boolean ack;

    public Ack(int term, boolean ack) {
        this.term = term;
        this.ack = ack;
    }
}
