package contract;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import status.MsgState;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
@AllArgsConstructor
public class PublishEvent<T> implements Serializable, MsgState {
    private int id;
    private T payload;
    private String topic;
    private Integer partition;
    private Integer status;

    public static PublishEvent<String> ok(int id) {
        return new PublishEvent<>(id, null, null, null, OK);
    }

    public static PublishEvent<String> fail(int id, String msg) {
        return new PublishEvent<>(id, msg, null, null, FAIL);
    }
}
